package org.ecotrack_project.repository;

import org.ecotrack_project.entity.PagoTotalEntity;
import org.ecotrack_project.enums.EstadoPagoTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoTotalRepository extends JpaRepository<PagoTotalEntity, Integer> {

    Optional<PagoTotalEntity> findBySolicitud_IdSolicitudAndEstado(int idSolicitud, EstadoPagoTotal estado);
    long countBySolicitud_IdSolicitud(int idSolicitud);
    Optional<PagoTotalEntity> findFirstBySolicitud_IdSolicitudOrderByIdPagoTotalDesc(int idSolicitud);
}
