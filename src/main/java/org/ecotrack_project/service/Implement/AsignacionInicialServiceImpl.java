
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
    private final PagoInicialRepository pagoInicialRepository;
    private final MedioPagoRepository medioPagoRepository;

    @Override
    @Transactional
    public ApiResponse<Integer> registrarAsignacionInicial(int idSolicitud, AsignacionInicialRequest request) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Optional<UsuarioEntity> usuarioOptional =
                usuarioRepository.findByUsername(username);

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario autenticado no encontrado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        if (usuario.getRol() != Rol.COORDINADOR &&
                usuario.getRol() != Rol.ADMINISTRADOR) {
            return ApiResponse.error("No está autorizado para realizar esta operación");
        }

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findByIdSolicitudAndEstado(
                        idSolicitud,
                        EstadoSolicitud.PENDIENTE
                );

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud pendiente no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        AsignacionInicialEntity asignacion = AsignacionInicialEntity.builder()
                .montoAsignadoInicial(request.getMontoAsignadoInicial())
                .fechaRegistro(LocalDateTime.now())
                .solicitud(solicitud)
                .coordinador(usuario)
                .build();

        AsignacionInicialEntity guardado =
                asignacionInicialRepository.save(asignacion);

        solicitud.setEstado(EstadoSolicitud.PAGO_INICIAL_PENDIENTE);
        solicitudRepository.save(solicitud);

        return ApiResponse.success(
                guardado.getIdAsignacionInicial(),
                "Se ha asignado el pago inicial correctamente"
        );
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
                .build();

        return ApiResponse.success(response, "Asignación inicial encontrada correctamente");
    }
}
