package org.ecotrack_project.repository;

import org.ecotrack_project.entity.DetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleRepository extends JpaRepository<DetalleEntity, Integer> {

    List<DetalleEntity> findByProgramacion_IdProgramacionOrderByIdDetalleAsc(int idProgramacion);
    Optional<DetalleEntity> findByIdDetalleAndProgramacion_IdProgramacion(int idDetalle, int idProgramacion);
    boolean existsByVehiculo_IdVehiculo(int idVehiculo);
    boolean existsByTransportista_IdUsuario(int idTransportista);

    List<DetalleEntity> findByProgramacion_IdProgramacionAndEstadoTrueOrderByIdDetalleAsc(int idProgramacion);
    Optional<DetalleEntity> findByIdDetalleAndProgramacion_IdProgramacionAndEstadoTrue(int idDetalle, int idProgramacion);
    boolean existsByProgramacion_IdProgramacionAndEstadoTrue(int idProgramacion);
}