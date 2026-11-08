
package org.ecotrack_project.repository;

import org.ecotrack_project.entity.ColaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColaRepository extends JpaRepository<ColaEntity, Integer> {

    List<ColaEntity> findByDetalle_Programacion_IdProgramacionAndEstadoTrueOrderByFechaRegistroAscIdColaAsc(int idProgramacion);
    long countByDetalle_Programacion_IdProgramacionAndEstadoTrue(int idProgramacion);
    Optional<ColaEntity> findByIdColaAndDetalle_Programacion_IdProgramacionAndEstadoTrue(int idCola, int idProgramacion);
    boolean existsByDetalle_IdDetalleAndEstadoTrue(int idDetalle);
    long countByDetalle_Programacion_IdProgramacion(int idProgramacion);
    List<ColaEntity> findByDetalle_Programacion_IdProgramacionOrderByFechaRegistroAscIdColaAsc(int idProgramacion);
}