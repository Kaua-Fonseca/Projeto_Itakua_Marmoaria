package com.itakua.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orcamento")
    @JsonProperty("id")
    private Long idOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusOrcamento status;

    // CORRIGIDO: renomeado de 'data' para 'dataCriacao' — alinha com o frontend
    @Column(name = "data_criacao")
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @Column(name = "valor_total", columnDefinition = "NUMERIC(10,2)")
    private Double valorTotal;

    @Column(name = "area_total", columnDefinition = "NUMERIC(10,3)")
    private Double areaTotal;

    @OneToMany(mappedBy = "orcamento", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemOrcamento> listaItens = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "material_unico_id")
    private Material materialUnico;

    @ManyToMany
    @JoinTable(name = "orcamento_materiais_pingadeira",
            joinColumns = @JoinColumn(name = "orcamento_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private List<Material> opcoesMateriaisPingadeira = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "orcamento_materiais_soleira",
            joinColumns = @JoinColumn(name = "orcamento_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private List<Material> opcoesMateriaisSoleira = new ArrayList<>();

    @Column(name = "prazo")
    private int prazo;

    @Column(name = "taxa_entrega")
    private Double taxaEntrega;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Transient
    public String getNumeroOrcamento() {
        return (idOrcamento == null) ? null : String.format("ORC-%04d", idOrcamento);
    }

    public void adicionarItem(ItemOrcamento item) {
        item.setOrcamento(this);
        listaItens.add(item);
    }

    public double calcularArea() {
        return listaItens.stream().mapToDouble(ItemOrcamento::calcularArea).sum();
    }

   
    public double calcularValorFinal(Material materialEscolhido) {
        if (materialEscolhido == null) return 0.0;
        double areaTotal = this.calcularArea();
        double valorItens = areaTotal * materialEscolhido.getPrecoMetro();
        return valorItens + (this.taxaEntrega != null ? this.taxaEntrega : 0.0);
    }

    public List<ItemOrcamento> listarPorTipo(TipoItem tipo) {
        return listaItens.stream()
                .filter(item -> item.getTipoItem() == tipo)
                .toList();
    }
}