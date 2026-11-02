package org.ecotrack_project.repository;

import org.ecotrack_project.entity.RecomendacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecomendacionRepository extends JpaRepository<RecomendacionEntity, Integer> {

    Optional<RecomendacionEntity> findBySolicitud_IdSolicitud(int idSolicitud);
}