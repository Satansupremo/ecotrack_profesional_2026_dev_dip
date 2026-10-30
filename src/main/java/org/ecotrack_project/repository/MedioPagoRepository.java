package org.ecotrack_project.repository;

import org.ecotrack_project.entity.MedioPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedioPagoRepository extends JpaRepository<MedioPagoEntity, Integer> {

    List<MedioPagoEntity> findByEstadoTrue();

}