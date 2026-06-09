package com.itakua.service;

import com.itakua.entity.ItemOrcamento;
import com.itakua.exception.*;
import com.itakua.repository.ItemOrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemOrcamentoService {

    private final ItemOrcamentoRepository itemRepository;

    public List<ItemOrcamento> listar() {
        return itemRepository.findAll();
    }

    public ItemOrcamento buscarPorId(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));
    }

    public ItemOrcamento salvar(ItemOrcamento item) {
        validar(item);
        return itemRepository.save(item);
    }

    public ItemOrcamento atualizar(Long id, ItemOrcamento item) {
        ItemOrcamento existente = buscarPorId(id);
        atualizarCampos(existente, item);
        validar(existente);
        return itemRepository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        itemRepository.deleteById(id);
    }

    private void atualizarCampos(ItemOrcamento existente, ItemOrcamento novo) {
        existente.setComprimento(novo.getComprimento());
        existente.setLargura(novo.getLargura());
        existente.setQuantidade(novo.getQuantidade());
        existente.setNumeradorPecas(novo.getNumeradorPecas());
        existente.setFrisoCortaPingo(novo.isFrisoCortaPingo()); // CORRIGIDO: era frisoCortaPringo
        existente.setTipoItem(novo.getTipoItem());
        existente.setComprimentoCorte(novo.getComprimentoCorte());
        existente.setLarguraCorte(novo.getLarguraCorte());
        existente.setTipoCorte(novo.getTipoCorte());
    }

    private void validar(ItemOrcamento item) {
        if (item.getComprimento() <= 0) {
            throw new ValidacaoException("Comprimento inválido");
        }
        if (item.getLargura() <= 0) {
            throw new ValidacaoException("Largura inválida");
        }
        if (item.getQuantidade() <= 0) {
            throw new ValidacaoException("Quantidade inválida");
        }
        if (item.getTipoItem() == null) {
            throw new ValidacaoException("Tipo obrigatório");
        }
    }
}
