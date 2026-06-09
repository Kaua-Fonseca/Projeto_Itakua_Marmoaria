package com.itakua.controller;

import com.itakua.entity.ItemOrcamento;
import com.itakua.service.ItemOrcamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item_orcamento")
@RequiredArgsConstructor
public class ItemOrcamentoController {

    private final ItemOrcamentoService itemOrcamentoService;

    @GetMapping
    public ResponseEntity<List<ItemOrcamento>> listar() {
        return ResponseEntity.ok(itemOrcamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemOrcamento> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(itemOrcamentoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ItemOrcamento> salvar(@RequestBody ItemOrcamento itemOrcamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemOrcamentoService.salvar(itemOrcamento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemOrcamento> atualizar(@PathVariable Long id,
                                                   @RequestBody ItemOrcamento itemOrcamento) {
        return ResponseEntity.ok(itemOrcamentoService.atualizar(id, itemOrcamento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        itemOrcamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
