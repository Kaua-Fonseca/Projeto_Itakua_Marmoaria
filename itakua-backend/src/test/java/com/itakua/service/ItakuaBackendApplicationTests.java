package com.itakua.service;

import com.itakua.entity.*;
import com.itakua.exception.*;
import com.itakua.repository.ClienteRepository;
import com.itakua.repository.MaterialRepository;
import com.itakua.repository.OrcamentoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {

	@Mock private OrcamentoRepository orcamentoRepository;
	@Mock private MaterialRepository materialRepository;
	@Mock private ClienteRepository clienteRepository;

	@InjectMocks private OrcamentoService orcamentoService;

	private Cliente cliente;
	private Material granito;

	@BeforeEach
	void setUp() {
		cliente = new Cliente(1L, "João Silva", "44999998888", "Rua A, 1");
		granito = new Material(1L, "Granito", 150.0, "Pedra natural");
	}

	// --- salvar e processamento ---

	@Test
	void salvar_deveAdicionarTresCentimetrosNoCorteQuandoZero() {
		ItemOrcamento item = criarItemValido();
		item.setComprimento(1.0);
		item.setLargura(0.20);
		item.setComprimentoCorte(0);
		item.setLarguraCorte(0);
		item.setTipoCorte(TipoCorte.INDIVIDUAL);

		Orcamento orc = criarOrcamentoValido(item);
		orc.setMaterialUnico(granito); // Material definido no nível do orçamento

		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		when(materialRepository.findById(1L)).thenReturn(Optional.of(granito));
		when(orcamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Orcamento resultado = orcamentoService.salvar(orc);

		assertThat(resultado.getListaItens().getFirst().getComprimentoCorte()).isEqualTo(1.03);
		assertThat(resultado.getListaItens().getFirst().getLarguraCorte()).isEqualTo(0.23);
	}

	@Test
	void salvar_conjunto_deveMultiplicarComprimentoPorQuantidade() {
		ItemOrcamento item = criarItemValido();
		item.setComprimento(1.0);
		item.setQuantidade(3);
		item.setComprimentoCorte(0);
		item.setTipoCorte(TipoCorte.CONJUNTO);

		Orcamento orc = criarOrcamentoValido(item);
		orc.setMaterialUnico(granito);

		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		when(materialRepository.findById(1L)).thenReturn(Optional.of(granito));
		when(orcamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Orcamento resultado = orcamentoService.salvar(orc);

		// Cálculo: (1.0 * 3) + 0.03 = 3.03
		assertThat(resultado.getListaItens().getFirst().getComprimentoCorte()).isEqualTo(3.03);
	}

	// --- validações ---

	@Test
	void salvar_semCliente_deveLancarValidacaoException() {
		Orcamento orc = new Orcamento();
		orc.setCliente(null);
		orc.setListaItens(List.of(criarItemValido()));

		assertThatThrownBy(() -> orcamentoService.salvar(orc))
				.isInstanceOf(ValidacaoException.class)
				.hasMessageContaining("Cliente obrigatório");
	}

	@Test
	void salvar_materialInexistente_deveLancarRecursoNaoEncontrado() {
		Orcamento orc = criarOrcamentoValido(criarItemValido());
		Material matInexistente = new Material();
		matInexistente.setId(99L);
		orc.setMaterialUnico(matInexistente);

		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		when(materialRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orcamentoService.salvar(orc))
				.isInstanceOf(RecursoNaoEncontradoException.class)
				.hasMessageContaining("Material");
	}

	@Test
	void salvar_devePersistirComStatusPendente_quandoStatusNaoInformado() {
		Orcamento orc = criarOrcamentoValido(criarItemValido());
		orc.setMaterialUnico(granito);
		orc.setStatus(null);

		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		when(materialRepository.findById(1L)).thenReturn(Optional.of(granito));
		when(orcamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Orcamento resultado = orcamentoService.salvar(orc);

		assertThat(resultado.getStatus()).isEqualTo(StatusOrcamento.PENDENTE);
	}

	@Test
	void calcularTotais_deveIncluirTaxaEntregaNoValorTotal() {
		ItemOrcamento item = criarItemValido();
		item.setComprimentoCorte(1.0);
		item.setLarguraCorte(1.0);
		item.setQuantidade(1);
		item.setTipoCorte(TipoCorte.INDIVIDUAL);

		Orcamento orc = criarOrcamentoValido(item);
		orc.setMaterialUnico(granito);
		orc.setTaxaEntrega(50.0);

		when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
		when(materialRepository.findById(1L)).thenReturn(Optional.of(granito));
		when(orcamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		Orcamento resultado = orcamentoService.salvar(orc);

		// (1.0 * 1.0 * 150.0) + 50.0 = 200.0
		assertThat(resultado.getValorTotal()).isEqualTo(200.0);
	}

	// --- helpers ---

	private ItemOrcamento criarItemValido() {
		ItemOrcamento item = new ItemOrcamento();
		item.setComprimento(1.0);
		item.setLargura(0.20);
		item.setQuantidade(1);
		item.setTipoItem(TipoItem.SOLEIRA);
		item.setTipoCorte(TipoCorte.INDIVIDUAL);
		// Material removido do item conforme nova arquitetura
		return item;
	}

	private Orcamento criarOrcamentoValido(ItemOrcamento item) {
		Orcamento orc = new Orcamento();
		Cliente c = new Cliente();
		c.setId(1L);
		orc.setCliente(c);
		orc.setListaItens(new ArrayList<>(List.of(item)));
		return orc;
	}
}