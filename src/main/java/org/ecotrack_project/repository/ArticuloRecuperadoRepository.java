package org.ecotrack_project.repository;

import org.ecotrack_project.entity.ArticuloRecuperadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticuloRecuperadoRepository extends JpaRepository<ArticuloRecuperadoEntity, Integer> {

    List<ArticuloRecuperadoEntity> findByCarga_IdCarga(int idCarga);
}