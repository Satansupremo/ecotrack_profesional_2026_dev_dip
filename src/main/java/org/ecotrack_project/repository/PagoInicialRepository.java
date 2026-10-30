package org.ecotrack_project.repository;

import org.ecotrack_project.entity.PagoInicialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoInicialRepository extends JpaRepository<PagoInicialEntity, Integer> {
}
