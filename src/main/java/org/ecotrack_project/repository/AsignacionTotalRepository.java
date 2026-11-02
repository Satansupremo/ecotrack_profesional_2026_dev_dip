package org.ecotrack_project.repository;

import org.ecotrack_project.entity.AsignacionTotalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsignacionTotalRepository extends JpaRepository<AsignacionTotalEntity, Integer> {

    Optional<AsignacionTotalEntity> findBySolicitud_IdSolicitud(int idSolicitud);
}