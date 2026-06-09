package com.itakua.service;

import com.itakua.entity.Cliente;
import com.itakua.exception.*;
import com.itakua.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // MELHORIA: injeção via construtor (sem @Autowired em campo)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado")); // MELHORIA: exceção tipada -> HTTP 404
    }

    public Cliente salvar(Cliente cliente) {
        validar(cliente);
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente cliente) {
        Cliente existente = buscarPorId(id);
        atualizarCampos(existente, cliente);
        validar(existente);
        return clienteRepository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        clienteRepository.deleteById(id);
    }

    private void validar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new ValidacaoException("Nome obrigatório"); // MELHORIA: exceção tipada -> HTTP 400
        }

        if (cliente.getTelefone() == null || cliente.getTelefone().isBlank()) {
            throw new ValidacaoException("Telefone obrigatório");
        }

        cliente.setTelefone(cliente.getTelefone().replaceAll("[^0-9]", ""));

        if (cliente.getTelefone().length() < 10 || cliente.getTelefone().length() > 11) {
            throw new ValidacaoException("Telefone inválido (deve ter 10 ou 11 dígitos)");
        }
    }

    private void atualizarCampos(Cliente existente, Cliente novo) {
        existente.setNome(novo.getNome());
        existente.setTelefone(novo.getTelefone());
        existente.setEndereco(novo.getEndereco());
    }
}
