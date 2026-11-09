package org.ecotrack_project.repository;

import org.ecotrack_project.entity.NotificacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<NotificacionEntity, Integer> {

    Page<NotificacionEntity> findByUsuario_UsernameOrderByIdNotificacionDesc(String username, Pageable pageable);
    long countByUsuario_UsernameAndEstadoFalse(String username);
    Optional<NotificacionEntity> findByIdNotificacionAndUsuario_Username(int idNotificacion, String username);
    long countByUsuario_Username(String username);
}