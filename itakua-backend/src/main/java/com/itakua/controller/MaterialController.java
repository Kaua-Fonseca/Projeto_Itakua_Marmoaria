package com.itakua.controller;

import com.itakua.entity.Material;
import com.itakua.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public ResponseEntity<List<Material>> listar() {
        return ResponseEntity.ok(materialService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(materialService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Material> salvar(@RequestBody @NonNull Material material) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.salvar(material));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> atualizar(@PathVariable Long id, @RequestBody Material material) {
        return ResponseEntity.ok(materialService.atualizar(id, material));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        materialService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
