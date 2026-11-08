package org.ecotrack_project.repository;

import org.ecotrack_project.entity.CargaEntity;
import org.ecotrack_project.enums.EstadoCarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargaRepository extends JpaRepository<CargaEntity, Integer> {

    List<CargaEntity> findByProgramacion_IdProgramacionOrderByFechaRegistroDesc(int idProgramacion);
    boolean existsByCola_IdCola(int idCola);
    List<CargaEntity> findByTransportista_IdUsuarioOrderByFechaRegistroDesc(int idTransportista);
    long countByProgramacion_IdProgramacion(int idProgramacion);
    Optional<CargaEntity> findByCola_IdCola(int idCola);
    List<CargaEntity> findByProgramacion_IdProgramacionAndEstado(int idProgramacion, EstadoCarga estado);
}