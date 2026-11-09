
package org.ecotrack_project.repository;

import jakarta.persistence.LockModeType;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<SolicitudEntity, Integer> {

    long countByUsuario_IdUsuario(int idUsuario);
    Page<SolicitudEntity> findByUsuario_IdUsuarioOrderByIdSolicitudDesc(int idUsuario, Pageable pageable);
    Optional<SolicitudEntity> findByIdSolicitudAndUsuario_IdUsuario(int idSolicitud, int idUsuario);
    long countByEstado(EstadoSolicitud estado);
    Page<SolicitudEntity> findByEstadoOrderByIdSolicitudDesc(EstadoSolicitud estado, Pageable pageable);
    Optional<SolicitudEntity> findByIdSolicitudAndEstado(int idSolicitud, EstadoSolicitud estado);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SolicitudEntity s WHERE s.idSolicitud = :idSolicitud")
    Optional<SolicitudEntity> findByIdForUpdate(@Param("idSolicitud") int idSolicitud);
    long countByEstadoIn(List<EstadoSolicitud> estados);
    Page<SolicitudEntity> findByEstadoInOrderByIdSolicitudDesc(List<EstadoSolicitud> estados, Pageable pageable);
}
