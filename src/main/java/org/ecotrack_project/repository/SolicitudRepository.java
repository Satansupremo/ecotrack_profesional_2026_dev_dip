package org.ecotrack_project.repository;

import org.ecotrack_project.entity.SolicitudEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudRepository extends JpaRepository<SolicitudEntity, Integer> {

    long countByUsuario_IdUsuario(int idUsuario);

    Page<SolicitudEntity> findByUsuario_IdUsuario(
            int idUsuario, Pageable pageable
    );
}
