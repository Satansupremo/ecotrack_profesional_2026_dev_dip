package org.ecotrack_project.repository;

import org.ecotrack_project.entity.PagoTotalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoFinalRepository extends JpaRepository<PagoTotalEntity, Integer> {
}
