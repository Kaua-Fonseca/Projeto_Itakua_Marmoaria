package com.itakua.repository;

import com.itakua.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository
        extends JpaRepository<Material, Long> {
}
