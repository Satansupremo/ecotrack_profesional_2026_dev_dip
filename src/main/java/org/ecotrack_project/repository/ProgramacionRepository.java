package org.ecotrack_project.repository;

import jakarta.persistence.LockModeType;
import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramacionRepository extends JpaRepository<ProgramacionEntity, Integer> {

    long countByJefeUnidad_IdUsuario(int idJefeUnidad);
    Optional<ProgramacionEntity> findFirstByJefeUnidad_IdUsuarioOrderByFechaRegistroDescIdProgramacionDesc(int idJefeUnidad);
    boolean existsBySolicitud_IdSolicitud(int idSolicitud);
    long countByFechaEjecucionGreaterThanEqualAndFechaEjecucionLessThan(LocalDateTime inicio, LocalDateTime fin);
    Page<ProgramacionEntity> findByFechaEjecucionGreaterThanEqualAndFechaEjecucionLessThanOrderByIdProgramacionDesc(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
    @Query("""
        SELECT COALESCE(SUM(r.cantidadUnidades), 0)
        FROM ProgramacionEntity p
        JOIN RecomendacionEntity r ON r.solicitud = p.solicitud
        WHERE p.jefeUnidad.idUsuario = :idJefeUnidad
        AND p.fechaEjecucion >= :inicio
        AND p.fechaEjecucion < :fin
        """)
    long sumarUnidadesRecomendadasPorJefeYPeriodo(
            @Param("idJefeUnidad") int idJefeUnidad,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
    Page<ProgramacionEntity> findByJefeUnidad_IdUsuarioOrderByIdProgramacionDesc(int idJefeUnidad, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProgramacionEntity p WHERE p.idProgramacion = :idProgramacion")
    Optional<ProgramacionEntity> findByIdForUpdate(
            @Param("idProgramacion") int idProgramacion
    );
    @Query("""
    SELECT COUNT(p)
    FROM ProgramacionEntity p
    WHERE EXISTS (
        SELECT d.idDetalle
        FROM DetalleEntity d
        WHERE d.programacion = p
        AND d.transportista.idUsuario = :idTransportista
        AND d.estado = true
    )
""")
    long countByTransportistaAsignado(
            @Param("idTransportista") int idTransportista
    );

    @Query(
            value = """
        SELECT p
        FROM ProgramacionEntity p
        WHERE EXISTS (
            SELECT d.idDetalle
            FROM DetalleEntity d
            WHERE d.programacion = p
            AND d.transportista.idUsuario = :idTransportista
            AND d.estado = true
        )
    """,
            countQuery = """
        SELECT COUNT(p)
        FROM ProgramacionEntity p
        WHERE EXISTS (
            SELECT d.idDetalle
            FROM DetalleEntity d
            WHERE d.programacion = p
            AND d.transportista.idUsuario = :idTransportista
            AND d.estado = true
        )
    """
    )
    Page<ProgramacionEntity> findByTransportistaAsignado(@Param("idTransportista") int idTransportista, Pageable pageable);

    long countByEstadoIn(List<EstadoProgramacion> estados);
    Page<ProgramacionEntity> findByEstadoIn(List<EstadoProgramacion> estados, Pageable pageable);
    long countByJefeUnidad_IdUsuarioAndEstado(
            int idJefeUnidad,
            EstadoProgramacion estado
    );

    long countByEstado(EstadoProgramacion estado);

    @Query("""
    SELECT COUNT(p)
    FROM ProgramacionEntity p
    WHERE p.estado = :estado
    AND EXISTS (
        SELECT d.idDetalle
        FROM DetalleEntity d
        WHERE d.programacion = p
        AND d.transportista.idUsuario = :idTransportista
        AND d.estado = true
    )
    """)
    long countByTransportistaAsignadoAndEstado(
            @Param("idTransportista") int idTransportista,
            @Param("estado") EstadoProgramacion estado
    );
    Optional<ProgramacionEntity> findFirstBySolicitud_IdSolicitudOrderByFechaRegistroDesc(int idSolicitud);
}