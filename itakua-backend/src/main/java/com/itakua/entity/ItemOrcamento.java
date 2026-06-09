package com.itakua.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_orcamento")
public class ItemOrcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_orcamento")
    @JsonBackReference
    private Orcamento orcamento;

    @Column(name = "comprimento")
    private double comprimento;

    @Column(name = "largura")
    private double largura;

    @Column(name = "numero_pecas")
    private int numeradorPecas;

    @Column(name = "friso_corta_pingo")
    private boolean frisoCortaPingo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item")
    private TipoItem tipoItem;

    @Column(name = "comprimento_corte", columnDefinition = "NUMERIC(10,3)")
    private double comprimentoCorte;

    @Column(name = "largura_corte", columnDefinition = "NUMERIC(10,3)")
    private double larguraCorte;

    @Column(name = "quantidade")
    private int quantidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_corte")
    private TipoCorte tipoCorte;


    public double calcularArea() {

        if(tipoCorte == TipoCorte.CONJUNTO) {
            return comprimentoCorte * larguraCorte;
        }

        return comprimentoCorte * larguraCorte * quantidade;
    }


}