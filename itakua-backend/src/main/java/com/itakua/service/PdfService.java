package com.itakua.service;

import com.itakua.entity.ItemOrcamento;
import com.itakua.entity.Orcamento;
import com.itakua.entity.TipoItem;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfService {



    private final TemplateEngine templateEngine;

    // --- CLASSE PARA OS MATERIAIS ---
    public static class MaterialCalculado {
        public final String nome;
        public final double valor;
        public MaterialCalculado(String nome, double valor) {
            this.nome = nome;
            this.valor = valor;
        }
    }

    // --- CLASSE PARA OS ITENS DA TABELA (A que estava faltando) ---
    public static class ItemComLabel {
        public final ItemOrcamento item;
        public final String labelNumero;
        public final boolean frisoCortaPingo;

        public ItemComLabel(ItemOrcamento item, int numeroInicio) {
            this.item = item;
            this.frisoCortaPingo = item.isFrisoCortaPingo();
            int qtd = item.getQuantidade();
            this.labelNumero = (qtd <= 1) ? String.valueOf(numeroInicio) : numeroInicio + " a " + (numeroInicio + qtd - 1);
        }
    }

    public byte[] gerarPdfOrcamento(Orcamento orcamento) {
        try {
            List<ItemOrcamento> pingadeiras = orcamento.listarPorTipo(TipoItem.PINGADEIRA);
            List<ItemOrcamento> soleiras = orcamento.listarPorTipo(TipoItem.SOLEIRA);

            int[] contador = {1};
            List<ItemComLabel> pingLabels = montarLabels(pingadeiras, contador);
            List<ItemComLabel> solLabels = montarLabels(soleiras, contador);

            List<MaterialCalculado> pingMateriais = orcamento.getOpcoesMateriaisPingadeira().stream()
                    .map(m -> new MaterialCalculado(m.getNome(), orcamento.calcularValorFinal(m)))
                    .collect(Collectors.toList());

            List<MaterialCalculado> solMateriais = orcamento.getOpcoesMateriaisSoleira().stream()
                    .map(m -> new MaterialCalculado(m.getNome(), orcamento.calcularValorFinal(m)))
                    .collect(Collectors.toList());

            Context context = new Context();
            context.setVariable("orcamento", orcamento);
            context.setVariable("pingLabels", pingLabels);
            context.setVariable("solLabels", solLabels);
            context.setVariable("pingMateriais", pingMateriais);
            context.setVariable("solMateriais", solMateriais);

            // Data formatada como String para garantir que apareçam os segundos
            context.setVariable("dataGeracao", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            context.setVariable("dataOrcamento", orcamento.getDataCriacao() != null ? orcamento.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "---");

            context.setVariable("areaPing", String.format("%.2f", pingadeiras.stream().mapToDouble(ItemOrcamento::calcularArea).sum()));
            context.setVariable("areaSol", String.format("%.2f", soleiras.stream().mapToDouble(ItemOrcamento::calcularArea).sum()));
            context.setVariable("valorTotal", String.format("%.2f", orcamento.getValorTotal() != null ? orcamento.getValorTotal() : 0.0));
            context.setVariable("prazo", (orcamento.getPrazo() > 0) ? orcamento.getPrazo() + " dias úteis" : "A combinar");

            String htmlPreenchido = templateEngine.process("pedido_impressao", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlPreenchido, "/");
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private List<ItemComLabel> montarLabels(List<ItemOrcamento> itens, int[] contador) {
        List<ItemComLabel> labels = new ArrayList<>();
        for (ItemOrcamento item : itens) {
            labels.add(new ItemComLabel(item, contador[0]));
            contador[0] += item.getQuantidade();
        }
        return labels;
    }
}