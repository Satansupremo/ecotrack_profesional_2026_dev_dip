package org.ecotrack_project.repository;

import jakarta.persistence.LockModeType;
import org.ecotrack_project.entity.ProgramacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProgramacionRepository extends JpaRepository<ProgramacionEntity, Integer> {

    long countByJefeUnidad_IdUsuario(int idJefeUnidad);
    Optional<ProgramacionEntity> findFirstByJefeUnidad_IdUsuarioOrderByFechaRegistroDescIdProgramacionDesc(int idJefeUnidad);
    boolean existsBySolicitud_IdSolicitud(int idSolicitud);
    long countByFechaEjecucionGreaterThanEqualAndFechaEjecucionLessThan(LocalDateTime inicio, LocalDateTime fin);
    Page<ProgramacionEntity> findByFechaEjecucionGreaterThanEqualAndFechaEjecucionLessThan(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
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
    Page<ProgramacionEntity> findByJefeUnidad_IdUsuario(int idJefeUnidad, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProgramacionEntity p WHERE p.idProgramacion = :idProgramacion")
    Optional<ProgramacionEntity> findByIdForUpdate(
            @Param("idProgramacion") int idProgramacion
    );
}