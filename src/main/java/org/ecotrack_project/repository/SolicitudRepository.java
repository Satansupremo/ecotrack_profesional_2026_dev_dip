
package org.ecotrack_project.repository;

import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<SolicitudEntity, Integer> {

    long countByUsuario_IdUsuario(int idUsuario);
    Page<SolicitudEntity> findByUsuario_IdUsuario(int idUsuario, Pageable pageable);
    Optional<SolicitudEntity> findByIdSolicitudAndUsuario_IdUsuario(int idSolicitud, int idUsuario);
    long countByEstado(EstadoSolicitud estado);
    Page<SolicitudEntity> findByEstado(EstadoSolicitud estado, Pageable pageable);
    Optional<SolicitudEntity> findByIdSolicitudAndEstado(int idSolicitud, EstadoSolicitud estado);
}
