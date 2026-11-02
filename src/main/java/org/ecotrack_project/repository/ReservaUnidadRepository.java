
package org.ecotrack_project.repository;

import org.ecotrack_project.entity.ReservaUnidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaUnidadRepository extends JpaRepository<ReservaUnidadEntity, Integer> {

    List<ReservaUnidadEntity> findByProgramacion_IdProgramacionOrderByIdReservaAsc(int idProgramacion);

    List<ReservaUnidadEntity> findByUsuarioReserva_IdUsuario(int idUsuarioReserva);

    Optional<ReservaUnidadEntity> findByIdReservaAndUsuarioReserva_IdUsuario(
            int idReserva,
            int idUsuarioReserva
    );

    boolean existsByVehiculo_IdVehiculo(int idVehiculo);

    boolean existsByTransportista_IdUsuario(int idTransportista);

    boolean existsByProgramacion_IdProgramacionAndUsuarioReserva_IdUsuario(
            int idProgramacion,
            int idUsuarioReserva
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM ReservaUnidadEntity r
        WHERE r.idReserva = :idReserva
    """)
    Optional<ReservaUnidadEntity> buscarPorIdConBloqueo(@Param("idReserva") int idReserva);

    List<ReservaUnidadEntity> findByFechaExpiracionLessThanEqual(LocalDateTime fechaActual);

    void deleteByProgramacion_IdProgramacionAndUsuarioReserva_IdUsuario(
            int idProgramacion,
            int idUsuarioReserva
    );
}
