package com.itakua.repository;

import com.itakua.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoRepository
        extends JpaRepository<Orcamento, Long> {
}
