package com.itakua.service;

import com.itakua.entity.*;
import com.itakua.exception.*;
import com.itakua.repository.ClienteRepository;
import com.itakua.repository.MaterialRepository;
import com.itakua.repository.OrcamentoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final MaterialRepository materialRepository;
    private final ClienteRepository clienteRepository;

    public List<Orcamento> findAll() {
        return orcamentoRepository.findAll();
    }

    public Orcamento buscarPorId(@NonNull Long id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado"));
    }

    @Transactional
    public Orcamento salvar(@NonNull Orcamento orcamento) {
        processarOrcamento(orcamento);
        definirStatusInicial(orcamento);
        return orcamentoRepository.save(orcamento);
    }

    @SuppressWarnings("null")
    @Transactional
    public Orcamento atualizar(Long id, Orcamento orcamento) {
        Orcamento existente = buscarPorId(id);
        atualizarCampos(existente, orcamento);
        processarOrcamento(existente);
        return orcamentoRepository.save(existente);
    }

    public void deletar(@NonNull Long id) {
        buscarPorId(id);
        orcamentoRepository.deleteById(id);
    }

    private void processarOrcamento(Orcamento orcamento) {
        validarCliente(orcamento);
        validarItens(orcamento);

        //  Carrega materiais reais do banco
        carregarMateriaisDoBanco(orcamento);

        //  Lógica de Propagação (Material Único "domina" as listas)
        if (orcamento.getMaterialUnico() != null) {
            orcamento.setOpcoesMateriaisPingadeira(List.of(orcamento.getMaterialUnico()));
            orcamento.setOpcoesMateriaisSoleira(List.of(orcamento.getMaterialUnico()));
        }

        //  Cálculos
        definirMedidasCorte(orcamento);
        orcamento.setAreaTotal(orcamento.calcularArea());

        // Se houver material único, já define o valor total definitivo
        if (orcamento.getMaterialUnico() != null) {
            orcamento.setValorTotal(orcamento.calcularValorFinal(orcamento.getMaterialUnico()));
        }

        //  Numeração de peças
        int contador = 1;
        for (ItemOrcamento item : orcamento.getListaItens()) {
            item.setNumeradorPecas(contador);
            contador += item.getQuantidade();
        }
    }

    private void carregarMateriaisDoBanco(Orcamento orc) {
        if (orc.getMaterialUnico() != null && orc.getMaterialUnico().getId() != null) {
            orc.setMaterialUnico(buscarMaterial(orc.getMaterialUnico().getId()));
        }
        carregarLista(orc.getOpcoesMateriaisPingadeira());
        carregarLista(orc.getOpcoesMateriaisSoleira());
    }

    @SuppressWarnings("null")
    private void carregarLista(List<Material> lista) {
        if (lista == null || lista.isEmpty()) return;
        List<Material> carregados = new ArrayList<>();
        for (Material m : lista) {
            carregados.add(buscarMaterial(m.getId()));
        }
        lista.clear();
        lista.addAll(carregados);
    }

    private Material buscarMaterial(@NonNull Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material ID " + id + " não encontrado"));
    }

    private void definirMedidasCorte(Orcamento orcamento) {
        for (ItemOrcamento item : orcamento.getListaItens()) {
            item.setOrcamento(orcamento);
            if (item.getComprimentoCorte() == 0) {
                double extra = (item.getTipoCorte() == TipoCorte.CONJUNTO) ? (item.getComprimento() * item.getQuantidade()) : item.getComprimento();
                item.setComprimentoCorte(extra + 0.03);
            }
            if (item.getLarguraCorte() == 0) {
                item.setLarguraCorte(item.getLargura() + 0.03);
            }
        }
    }

    @SuppressWarnings("null")
    private void validarCliente(Orcamento orc) {
        if (orc.getCliente() == null || orc.getCliente().getId() == null) throw new ValidacaoException("Cliente obrigatório");
        orc.setCliente(clienteRepository.findById(orc.getCliente().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado")));
    }

    private void validarItens(Orcamento orc) {
        if (orc.getListaItens() == null || orc.getListaItens().isEmpty()) throw new ValidacaoException("O orçamento deve possuir ao menos um item.");
    }

    private void definirStatusInicial(Orcamento orc) {
        if (orc.getStatus() == null) orc.setStatus(StatusOrcamento.PENDENTE);
    }

    private void atualizarCampos(Orcamento existente, Orcamento novo) {
        existente.setCliente(novo.getCliente());
        existente.setListaItens(novo.getListaItens());
        existente.setPrazo(novo.getPrazo());
        existente.setTaxaEntrega(novo.getTaxaEntrega());
        existente.setMaterialUnico(novo.getMaterialUnico());
        existente.setOpcoesMateriaisPingadeira(novo.getOpcoesMateriaisPingadeira());
        existente.setOpcoesMateriaisSoleira(novo.getOpcoesMateriaisSoleira());
        existente.setObservacoes(novo.getObservacoes());
        if (novo.getStatus() != null) existente.setStatus(novo.getStatus());
    }
}