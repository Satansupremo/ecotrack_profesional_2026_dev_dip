package org.ecotrack_project.repository;

import org.ecotrack_project.entity.AsignacionInicialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionInicialRepository extends JpaRepository<AsignacionInicialEntity, Integer> {
}