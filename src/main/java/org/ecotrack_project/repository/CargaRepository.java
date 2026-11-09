package org.ecotrack_project.repository;

import org.ecotrack_project.dto.response.CantidadCargasResponse;
import org.ecotrack_project.entity.CargaEntity;
import org.ecotrack_project.enums.EstadoCarga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CargaRepository extends JpaRepository<CargaEntity, Integer> {

    // --- NUEVA CONSULTA DE REPORTE OPERATIVO ---
    @Query("""
        SELECT new org.ecotrack_project.dto.response.CantidadCargasResponse(
            u.idUsuario,
            CONCAT(u.nombre, ' ', u.apellido),
            u.dni,
            u.rol,
            COUNT(c)
        )
        FROM CargaEntity c
        JOIN c.operario o
        JOIN c.transportista t
        JOIN UsuarioEntity u ON (u.idUsuario = o.idUsuario OR u.idUsuario = t.idUsuario)
        WHERE u.idUsuario = :idEmpleado
        AND c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin
        GROUP BY u.idUsuario, u.nombre, u.apellido, u.dni, u.rol
    """)
    Optional<CantidadCargasResponse> contarCargasPorEmpleadoYFechas(
            @Param("idEmpleado") int idEmpleado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query(value = """
        SELECT new org.ecotrack_project.dto.response.CantidadCargasResponse(
            u.idUsuario,
            CONCAT(u.nombre, ' ', u.apellido),
            u.dni,
            u.rol,
            COUNT(c)
        )
        FROM CargaEntity c
        JOIN c.operario o
        JOIN c.transportista t
        JOIN UsuarioEntity u ON (u.idUsuario = o.idUsuario OR u.idUsuario = t.idUsuario)
        WHERE c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin
        GROUP BY u.idUsuario, u.nombre, u.apellido, u.dni, u.rol
    """,
            countQuery = """
        SELECT COUNT(DISTINCT u.idUsuario)
        FROM CargaEntity c
        JOIN c.operario o
        JOIN c.transportista t
        JOIN UsuarioEntity u ON (u.idUsuario = o.idUsuario OR u.idUsuario = t.idUsuario)
        WHERE c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin
    """)
    Page<CantidadCargasResponse> obtenerRendimientoGeneralCargasPaginado(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
    // --- MÉTODOS EXISTENTES MANTENIDOS INTACTOS ---
    List<CargaEntity> findByProgramacion_IdProgramacionOrderByFechaRegistroDesc(int idProgramacion);
    boolean existsByCola_IdCola(int idCola);
    List<CargaEntity> findByTransportista_IdUsuarioOrderByFechaRegistroDesc(int idTransportista);
    long countByProgramacion_IdProgramacion(int idProgramacion);
    Optional<CargaEntity> findByCola_IdCola(int idCola);
    List<CargaEntity> findByProgramacion_IdProgramacionAndEstado(int idProgramacion, EstadoCarga estado);
}
