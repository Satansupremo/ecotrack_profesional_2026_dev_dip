package org.ecotrack_project.repository;

import jakarta.persistence.LockModeType;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    Optional<UsuarioEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByCorreo(String correo);
    boolean existsByDni(String dni);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UsuarioEntity u WHERE u.username = :username")
    Optional<UsuarioEntity> findByUsernameForUpdate(@Param("username") String username);
    List<UsuarioEntity> findByRolAndEstadoTrue(Rol rol);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UsuarioEntity> findFirstByRolAndEstadoTrueAndSituacionOrderByIdUsuarioAsc(Rol rol, SituacionUsuario situacion);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT u
    FROM UsuarioEntity u
    WHERE u.rol = :rol
    AND u.estado = true
    AND u.situacion = :situacion
    ORDER BY u.idUsuario ASC
""")
    List<UsuarioEntity> buscarTransportistasDisponiblesConBloqueo(
            @Param("rol") Rol rol,
            @Param("situacion") SituacionUsuario situacion
    );

}