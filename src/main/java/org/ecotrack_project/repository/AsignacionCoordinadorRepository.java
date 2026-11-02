package org.ecotrack_project.repository;

import org.ecotrack_project.entity.AsignacionCoordinadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsignacionCoordinadorRepository extends JpaRepository<AsignacionCoordinadorEntity, Integer> {

    boolean existsByCoordinador_IdUsuario(int idCoordinador);
    Optional<AsignacionCoordinadorEntity> findBySolicitud_IdSolicitud(int idSolicitud);
    boolean existsBySolicitud_IdSolicitud(int idSolicitud);
}