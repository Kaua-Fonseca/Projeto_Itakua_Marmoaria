package com.itakua.controller;

import com.itakua.entity.Orcamento;
import com.itakua.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orcamento")
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;
    private final PdfService pdfService;

    @GetMapping
    public ResponseEntity<List<Orcamento>> listar() {
        return ResponseEntity.ok(orcamentoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orcamento> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.buscarPorId(id));
    }

    //Gera e entrega o PDF direto para o navegador
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable Long id) {
        // 1. Aproveita o método que você já tem para buscar o orçamento do banco
        Orcamento orcamento = orcamentoService.buscarPorId(id);

        // 2. Transforma os dados desse orçamento no arquivo PDF (em bytes)
        byte[] pdfBytes = pdfService.gerarPdfOrcamento(orcamento);

        // 3. Configura os cabeçalhos para o navegador entender que é um download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        // Define o nome do arquivo baixado (Ex: pedido_1.pdf)
        String nomeArquivo = "pedido_" + id + ".pdf";
        headers.setContentDispositionFormData("attachment", nomeArquivo);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping
    public ResponseEntity<Orcamento> salvar(@RequestBody Orcamento orcamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orcamentoService.salvar(orcamento));
    }

    // CORRIGIDO: era @PutMapping sem "/{id}" — causava NullPointerException em runtime
    @PutMapping("/{id}")
    public ResponseEntity<Orcamento> atualizar(@PathVariable Long id, @RequestBody Orcamento orcamento) {
        return ResponseEntity.ok(orcamentoService.atualizar(id, orcamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        orcamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
