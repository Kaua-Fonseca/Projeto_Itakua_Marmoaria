package com.itakua.service;

import com.itakua.entity.Material;
import com.itakua.exception.*;
import com.itakua.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    // CORRIGIDO: antes retornava null, agora lança exceção tipada -> HTTP 404
    public Material buscarPorId(@NonNull Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material não encontrado"));
    }

    public Material salvar(@NonNull Material material) {
        validar(material);
        return materialRepository.save(material);
    }

    @SuppressWarnings("null")
    public Material atualizar(Long id, Material material) {
        Material existente = buscarPorId(id);
        atualizarCampos(existente, material);
        validar(existente);
        return materialRepository.save(existente);
    }

    @SuppressWarnings("null")
    public void deletar(Long id) {
        buscarPorId(id); // lança 404 se não existir
        materialRepository.deleteById(id);
    }

    private void validar(Material material) {
        if (material.getNome() == null || material.getNome().isBlank()) {
            throw new ValidacaoException("Nome do material é obrigatório");
        }
        if (material.getPrecoMetro() == null || material.getPrecoMetro() <= 0) {
            throw new ValidacaoException("Preço por metro inválido");
        }
    }

    private void atualizarCampos(Material existente, Material novo) {
        existente.setNome(novo.getNome());
        existente.setPrecoMetro(novo.getPrecoMetro());
        existente.setObservacao(novo.getObservacao());
    }
}
