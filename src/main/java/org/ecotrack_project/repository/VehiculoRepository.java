package org.ecotrack_project.repository;

import jakarta.persistence.LockModeType;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.enums.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<VehiculoEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VehiculoEntity> findFirstByTipoAndEstadoTrueAndSituacionOrderByIdVehiculoAsc(TipoVehiculo tipo, SituacionVehiculo situacion);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT v
    FROM VehiculoEntity v
    WHERE v.tipo = :tipo
    AND v.estado = true
    AND v.situacion = :situacion
    ORDER BY v.idVehiculo ASC
""")
    List<VehiculoEntity> buscarVehiculosDisponiblesConBloqueo(
            @Param("tipo") TipoVehiculo tipo,
            @Param("situacion") SituacionVehiculo situacion
    );

}