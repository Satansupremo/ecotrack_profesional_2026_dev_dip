
package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.AsignacionInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.AsignacionInicialResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.service.AsignacionInicialService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsignacionInicialServiceImpl implements AsignacionInicialService {

    private final AsignacionInicialRepository asignacionInicialRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    @Transactional
    public ApiResponse<Integer> registrarAsignacionInicial(
            int idSolicitud,
            AsignacionInicialRequest request) {

        ValidacionAsignacionInicial validacion = validarCondiciones(idSolicitud);

        if (validacion.error() != null) {
            return ApiResponse.error(validacion.error());
        }

        UsuarioEntity usuario = validacion.usuario();
        SolicitudEntity solicitud = validacion.solicitud();

        AsignacionInicialEntity asignacion = AsignacionInicialEntity.builder()
                .montoAsignadoInicial(request.getMontoAsignadoInicial())
                .razon(request.getRazon())
                .fechaRegistro(LocalDateTime.now())
                .solicitud(solicitud)
                .coordinador(usuario)
                .build();

        AsignacionInicialEntity guardado =
                asignacionInicialRepository.save(asignacion);

        solicitud.setEstado(EstadoSolicitud.PAGO_INICIAL_PENDIENTE);
        solicitudRepository.save(solicitud);

        NotificacionEntity notificacion = NotificacionEntity.builder()
                .descripcion(
                        "Se ha establecido un monto inicial de S/ " +
                                request.getMontoAsignadoInicial().setScale(2) +
                                " para la verificación de tu solicitud N.º " + idSolicitud +
                                ". Este monto corresponde a una estimación de los gastos necesarios para que el coordinador se traslade a la ubicación indicada y compruebe la validez de la solicitud. " +
                                "La asignación no representa una confirmación definitiva del servicio."
                )
                .estado(false)
                .usuario(solicitud.getUsuario())
                .build();

        notificacionRepository.save(notificacion);

        return ApiResponse.success(
                guardado.getIdAsignacionInicial(),
                "Se ha asignado el pago inicial correctamente"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Boolean> validarAsignacionInicial(int idSolicitud) {

        ValidacionAsignacionInicial validacion = validarCondiciones(idSolicitud);

        if (validacion.error() != null) {
            return ApiResponse.error(validacion.error());
        }

        return ApiResponse.success(true, "Los datos son válidos");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AsignacionInicialResponse> obtenerAsignacionInicial(int idSolicitud) {

        Optional<AsignacionInicialEntity> asignacionOptional =
                asignacionInicialRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error("No se encontró una asignación inicial para esta solicitud");
        }

        AsignacionInicialEntity asignacion = asignacionOptional.get();

        AsignacionInicialResponse response = AsignacionInicialResponse.builder()
                .idAsignacionInicial(asignacion.getIdAsignacionInicial())
                .montoAsignadoInicial(asignacion.getMontoAsignadoInicial())
                .razon(asignacion.getRazon())
                .build();

        return ApiResponse.success(response, "Asignación inicial encontrada correctamente");
    }

    private ValidacionAsignacionInicial validarCondiciones(int idSolicitud) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Optional<UsuarioEntity> usuarioOptional =
                usuarioRepository.findByUsername(username);

        if (usuarioOptional.isEmpty()) {
            return new ValidacionAsignacionInicial(null, null,
                    "Usuario autenticado no encontrado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        if (usuario.getRol() != Rol.COORDINADOR &&
                usuario.getRol() != Rol.ADMINISTRADOR) {
            return new ValidacionAsignacionInicial(null, null,
                    "No está autorizado para realizar esta operación");
        }

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findByIdSolicitudAndEstado(
                        idSolicitud,
                        EstadoSolicitud.PENDIENTE
                );

        if (solicitudOptional.isEmpty()) {
            return new ValidacionAsignacionInicial(null, null,
                    "Solicitud pendiente no encontrada");
        }

        return new ValidacionAsignacionInicial(
                usuario,
                solicitudOptional.get(),
                null
        );
    }

    private record ValidacionAsignacionInicial(
            UsuarioEntity usuario,
            SolicitudEntity solicitud,
            String error
    ) {}
}
