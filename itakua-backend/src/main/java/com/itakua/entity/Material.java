package com.itakua.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "preco_metro", nullable = false)
    private Double precoMetro;

    @Column(name = "observacao")
    private String observacao;
}