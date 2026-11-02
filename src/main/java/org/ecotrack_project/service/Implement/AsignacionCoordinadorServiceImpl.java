
package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.service.AsignacionCoordinadorService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsignacionCoordinadorServiceImpl implements AsignacionCoordinadorService {

    private final AsignacionCoordinadorRepository asignacionCoordinadorRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ApiResponse<Integer> tomarSolicitud(int idSolicitud) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Optional<UsuarioEntity> usuarioOptional =
                usuarioRepository.findByUsernameForUpdate(username);

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario autenticado no encontrado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        if (usuario.getRol() != Rol.COORDINADOR) {
            return ApiResponse.error("No está autorizado para realizar esta operación");
        }

        if (asignacionCoordinadorRepository.existsByCoordinador_IdUsuario(
                usuario.getIdUsuario())) {
            return ApiResponse.error(
                    "Ya tienes una solicitud en proceso de verificación"
            );
        }

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findByIdForUpdate(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_INICIAL_CONFIRMADO) {
            return ApiResponse.error(
                    "La solicitud no está disponible para ser tomada"
            );
        }

        if (asignacionCoordinadorRepository.existsBySolicitud_IdSolicitud(idSolicitud)) {
            return ApiResponse.error(
                    "La solicitud ya fue tomada por otro coordinador"
            );
        }

        AsignacionCoordinadorEntity asignacion =
                AsignacionCoordinadorEntity.builder()
                        .solicitud(solicitud)
                        .coordinador(usuario)
                        .build();

        AsignacionCoordinadorEntity guardado =
                asignacionCoordinadorRepository.save(asignacion);

        solicitud.setEstado(EstadoSolicitud.EN_VERIFICACION);
        solicitudRepository.save(solicitud);

        return ApiResponse.success(
                guardado.getIdAsignacionCoordinador(),
                "Has tomado la solicitud correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> cancelarSeleccion(int idSolicitud) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Optional<UsuarioEntity> usuarioOptional =
                usuarioRepository.findByUsernameForUpdate(username);

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario autenticado no encontrado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        if (usuario.getRol() != Rol.COORDINADOR) {
            return ApiResponse.error("No está autorizado para realizar esta operación");
        }

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findByIdForUpdate(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud en verificación no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.EN_VERIFICACION) {
            return ApiResponse.error("Solicitud en verificación no encontrada");
        }

        Optional<AsignacionCoordinadorEntity> asignacionOptional =
                asignacionCoordinadorRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error("No se encontró la asignación de la solicitud");
        }

        AsignacionCoordinadorEntity asignacion = asignacionOptional.get();

        if (asignacion.getCoordinador().getIdUsuario() != usuario.getIdUsuario()) {
            return ApiResponse.error(
                    "No puedes cancelar una solicitud asignada a otro coordinador"
            );
        }

        asignacionCoordinadorRepository.delete(asignacion);

        solicitud.setEstado(EstadoSolicitud.PAGO_INICIAL_CONFIRMADO);
        solicitudRepository.save(solicitud);

        return ApiResponse.success(
                null,
                "Has liberado la solicitud correctamente"
        );
    }
}
