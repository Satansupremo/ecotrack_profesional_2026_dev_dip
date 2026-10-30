
package org.ecotrack_project.repository;

import org.ecotrack_project.entity.PagoInicialEntity;
import org.ecotrack_project.enums.EstadoPagoInicial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoInicialRepository extends JpaRepository<PagoInicialEntity, Integer> {

    Optional<PagoInicialEntity> findBySolicitud_IdSolicitud(int idSolicitud);
    Optional<PagoInicialEntity> findBySolicitud_IdSolicitudAndEstado(int idSolicitud, EstadoPagoInicial estado);
    long countBySolicitud_IdSolicitud(int idSolicitud);
}
