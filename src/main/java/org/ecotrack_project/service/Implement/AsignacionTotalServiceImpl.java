
package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.AsignacionTotalRequest;
import org.ecotrack_project.dto.request.RecomendacionRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.AsignacionTotalResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoPagoInicial;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.service.AsignacionTotalService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsignacionTotalServiceImpl implements AsignacionTotalService {

    private final AsignacionTotalRepository asignacionTotalRepository;
    private final RecomendacionRepository recomendacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoInicialRepository pagoInicialRepository;
    private final NotificacionRepository notificacionRepository;
    private final AsignacionCoordinadorRepository asignacionCoordinadorRepository;


    @Override
    @Transactional
    public ApiResponse<Integer> registrarAsignacionTotal(int idSolicitud, AsignacionTotalRequest asignacionRequest, RecomendacionRequest recomendacionRequest) {

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

        Optional<AsignacionCoordinadorEntity> asignacionCoordinadorOptional =
                asignacionCoordinadorRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionCoordinadorOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró una asignación de coordinador para esta solicitud"
            );
        }

        AsignacionCoordinadorEntity asignacionCoordinador =
                asignacionCoordinadorOptional.get();

        if (asignacionCoordinador.getCoordinador().getIdUsuario()
                != usuario.getIdUsuario()) {
            return ApiResponse.error(
                    "No estás autorizado para asignar el pago total de esta solicitud"
            );
        }

        if (asignacionTotalRepository.findBySolicitud_IdSolicitud(idSolicitud).isPresent()) {
            return ApiResponse.error("La solicitud ya cuenta con una asignación total");
        }

        Optional<PagoInicialEntity> pagoInicialOptional =
                pagoInicialRepository.findBySolicitud_IdSolicitudAndEstado(
                        idSolicitud,
                        EstadoPagoInicial.CONFIRMADO
                );

        if (pagoInicialOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró un pago inicial confirmado para esta solicitud"
            );
        }

        BigDecimal montoDescontado = pagoInicialOptional.get().getMonto();

        BigDecimal montoAsignadoTotal = asignacionRequest.getMontoAsignadoTotal();

        if (montoAsignadoTotal.compareTo(montoDescontado) <= 0) {
            return ApiResponse.error(
                    "El monto total asignado debe ser mayor al pago inicial confirmado"
            );
        }

        BigDecimal montoPorCobrar = montoAsignadoTotal.subtract(montoDescontado);

        AsignacionTotalEntity asignacion = AsignacionTotalEntity.builder()
                .montoAsignadoTotal(montoAsignadoTotal)
                .montoDescontado(montoDescontado)
                .montoPorCobrar(montoPorCobrar)
                .fechaRegistro(LocalDateTime.now())
                .razon(asignacionRequest.getRazon())
                .solicitud(solicitud)
                .coordinador(usuario)
                .build();

        AsignacionTotalEntity guardado =
                asignacionTotalRepository.save(asignacion);

        RecomendacionEntity recomendacion = RecomendacionEntity.builder()
                .cantidadUnidades(recomendacionRequest.getCantidadUnidades())
                .razonCantidadUnidades(recomendacionRequest.getRazonCantidadUnidades())
                .equipoSeguridad(recomendacionRequest.getEquipoSeguridad())
                .otrasRecomendaciones(recomendacionRequest.getOtrasRecomendaciones())
                .solicitud(solicitud)
                .build();

        recomendacionRepository.save(recomendacion);

        solicitud.setEstado(EstadoSolicitud.PAGO_TOTAL_PENDIENTE);
        solicitudRepository.save(solicitud);

        NotificacionEntity notificacion = NotificacionEntity.builder()
                .descripcion(
                        "La solicitud N.º " + idSolicitud +
                                " tiene un descuento de S/ " + montoDescontado.setScale(2) +
                                " correspondiente al pago inicial confirmado. " +
                                "El monto por cobrar es de S/ " + montoPorCobrar.setScale(2) + "."
                )
                .estado(false)
                .usuario(solicitud.getUsuario())
                .build();

        notificacionRepository.save(notificacion);

        asignacionCoordinadorRepository.delete(asignacionCoordinador);

        return ApiResponse.success(
                guardado.getIdAsignacionTotal(),
                "Se ha asignado el pago total y las recomendaciones correctamente"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AsignacionTotalResponse> obtenerAsignacionTotal(int idSolicitud) {

        Optional<AsignacionTotalEntity> asignacionOptional =
                asignacionTotalRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró una asignación total para esta solicitud"
            );
        }

        AsignacionTotalEntity asignacion = asignacionOptional.get();

        AsignacionTotalResponse response = AsignacionTotalResponse.builder()
                .idAsignacionTotal(asignacion.getIdAsignacionTotal())
                .montoAsignadoTotal(asignacion.getMontoAsignadoTotal())
                .montoPorCobrar(asignacion.getMontoPorCobrar())
                .razon(asignacion.getRazon())
                .build();

        return ApiResponse.success(
                response,
                "Asignación total encontrada correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> verificarAsignacionTotal(int idSolicitud) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Optional<UsuarioEntity> usuarioOptional =
                usuarioRepository.findByUsernameForUpdate(username);

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario autenticado no encontrado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        if (usuario.getRol() != Rol.COORDINADOR) {
            return ApiResponse.error(
                    "No está autorizado para realizar esta operación"
            );
        }

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findByIdForUpdate(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.EN_VERIFICACION) {
            return ApiResponse.error(
                    "La solicitud no se encuentra en estado de verificación, así que no puedo asignar el pago total."
            );
        }

        Optional<AsignacionCoordinadorEntity> asignacionOptional =
                asignacionCoordinadorRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró una asignación de coordinador para esta solicitud"
            );
        }

        AsignacionCoordinadorEntity asignacion = asignacionOptional.get();

        if (asignacion.getCoordinador().getIdUsuario() != usuario.getIdUsuario()) {
            return ApiResponse.error(
                    "No estás autorizado para asignar el pago total de esta solicitud"
            );
        }

        if (asignacionTotalRepository.findBySolicitud_IdSolicitud(idSolicitud).isPresent()) {
            return ApiResponse.error(
                    "La solicitud ya cuenta con una asignación total"
            );
        }

        return ApiResponse.success(
                null,
                "Puedes continuar con la asignación del pago total"
        );
    }

}
