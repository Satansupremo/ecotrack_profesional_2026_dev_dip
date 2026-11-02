package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.PagoTotalRequest;
import org.ecotrack_project.dto.request.RechazoPagoTotalRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.PagoTotalDetalleResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoPagoTotal;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.service.PagoTotalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PagoTotalServiceImpl implements PagoTotalService {

    private final SolicitudRepository solicitudRepository;
    private final AsignacionTotalRepository asignacionTotalRepository;
    private final PagoTotalRepository pagoTotalRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    @Transactional
    public ApiResponse<Integer> registrarPagoTotal(int idSolicitud, PagoTotalRequest request) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_TOTAL_PENDIENTE) {
            return ApiResponse.error("La solicitud no está pendiente de registrar el pago final");
        }

        Optional<AsignacionTotalEntity> asignacionOptional =
                asignacionTotalRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error("No se encontró la asignación total de esta solicitud");
        }

        AsignacionTotalEntity asignacion = asignacionOptional.get();

        Optional<MedioPagoEntity> medioPagoOptional =
                medioPagoRepository.findById(request.getIdMedioPago());

        if (medioPagoOptional.isEmpty() || !medioPagoOptional.get().isEstado()) {
            return ApiResponse.error("Medio de pago no encontrado o no disponible");
        }

        PagoTotalEntity pago = PagoTotalEntity.builder()
                .monto(asignacion.getMontoPorCobrar())
                .fechaOperacion(request.getFechaOperacion())
                .horaOperacion(request.getHoraOperacion())
                .numeroOperacion(request.getNumeroOperacion())
                .estado(EstadoPagoTotal.PENDIENTE)
                .medioPago(medioPagoOptional.get())
                .solicitud(solicitud)
                .build();

        PagoTotalEntity guardado = pagoTotalRepository.save(pago);

        solicitud.setEstado(EstadoSolicitud.PAGO_TOTAL_EFECTUADO);
        solicitudRepository.save(solicitud);

        return ApiResponse.success(
                guardado.getIdPagoTotal(),
                "Pago final registrado correctamente. Pendiente de confirmación"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagoTotalDetalleResponse> obtenerDetallePagoTotal(int idSolicitud) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_TOTAL_EFECTUADO) {
            return ApiResponse.error(
                    "La solicitud no se encuentra pendiente de revisión del pago final"
            );
        }

        Optional<AsignacionTotalEntity> asignacionOptional =
                asignacionTotalRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró la asignación total de esta solicitud"
            );
        }

        Optional<PagoTotalEntity> pagoOptional =
                pagoTotalRepository.findBySolicitud_IdSolicitudAndEstado(
                        idSolicitud,
                        EstadoPagoTotal.PENDIENTE
                );

        if (pagoOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró un pago final pendiente de revisión"
            );
        }

        AsignacionTotalEntity asignacion = asignacionOptional.get();
        PagoTotalEntity pago = pagoOptional.get();

        PagoTotalDetalleResponse response = PagoTotalDetalleResponse.builder()
                .idSolicitud(solicitud.getIdSolicitud())
                .tituloSolicitud(solicitud.getTitulo())
                .fechaRegistroSolicitud(solicitud.getFechaRegistro())

                .idAsignacionTotal(asignacion.getIdAsignacionTotal())
                .montoAsignadoTotal(asignacion.getMontoAsignadoTotal())
                .montoDescontado(asignacion.getMontoDescontado())
                .montoPorCobrar(asignacion.getMontoPorCobrar())
                .fechaRegistroAsignacion(asignacion.getFechaRegistro())

                .idPagoTotal(pago.getIdPagoTotal())
                .montoPago(pago.getMonto())
                .fechaOperacion(pago.getFechaOperacion())
                .horaOperacion(pago.getHoraOperacion())
                .numeroOperacion(pago.getNumeroOperacion())
                .estadoPago(pago.getEstado().name())

                .nombreMedioPago(pago.getMedioPago().getNombre())
                .build();

        return ApiResponse.success(
                response,
                "Detalle del pago final obtenido correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> confirmarPagoTotal(int idSolicitud) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_TOTAL_EFECTUADO) {
            return ApiResponse.error(
                    "La solicitud no está pendiente de confirmación del pago final"
            );
        }

        Optional<PagoTotalEntity> pagoOptional =
                pagoTotalRepository.findBySolicitud_IdSolicitudAndEstado(
                        idSolicitud,
                        EstadoPagoTotal.PENDIENTE
                );

        if (pagoOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró un pago final pendiente de confirmación"
            );
        }

        PagoTotalEntity pago = pagoOptional.get();

        pago.setEstado(EstadoPagoTotal.CONFIRMADO);
        pagoTotalRepository.save(pago);

        solicitud.setEstado(EstadoSolicitud.PAGO_TOTAL_CONFIRMADO);
        solicitudRepository.save(solicitud);

        NotificacionEntity notificacion = NotificacionEntity.builder()
                .descripcion(
                        "Código de solicitud: " + solicitud.getIdSolicitud() + "\n\n" +
                                "Su pago final ha sido confirmado correctamente."
                )
                .estado(false)
                .usuario(solicitud.getUsuario())
                .build();

        notificacionRepository.save(notificacion);

        return ApiResponse.success(
                pago.getIdPagoTotal(),
                "Pago final confirmado correctamente. Se notificó al usuario"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> rechazarPagoTotal(
            int idSolicitud,
            RechazoPagoTotalRequest request) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_TOTAL_EFECTUADO) {
            return ApiResponse.error(
                    "La solicitud no está pendiente de revisión del pago final"
            );
        }

        Optional<PagoTotalEntity> pagoOptional =
                pagoTotalRepository.findBySolicitud_IdSolicitudAndEstado(
                        idSolicitud,
                        EstadoPagoTotal.PENDIENTE
                );

        if (pagoOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró un pago final pendiente de rechazo"
            );
        }

        PagoTotalEntity pago = pagoOptional.get();

        pago.setEstado(EstadoPagoTotal.RECHAZADO);
        pagoTotalRepository.saveAndFlush(pago);

        long cantidadIntentos =
                pagoTotalRepository.countBySolicitud_IdSolicitud(idSolicitud);

        boolean limiteAlcanzado =
                cantidadIntentos >= Initialize.CANTIDAD_INTENTO_PAGO_FINAL;

        if (limiteAlcanzado) {

            solicitud.setEstado(EstadoSolicitud.RECHAZADO);

            NotificacionEntity notificacion = NotificacionEntity.builder()
                    .descripcion(
                            "Código de solicitud: " + solicitud.getIdSolicitud() + "\n\n" +
                                    "Su solicitud ha sido rechazada definitivamente porque alcanzó " +
                                    "el límite máximo de intentos de pago final.\n\n" +
                                    "Motivo del último rechazo: " + request.getDescripcion()
                    )
                    .estado(false)
                    .usuario(solicitud.getUsuario())
                    .build();

            notificacionRepository.save(notificacion);

        } else {

            solicitud.setEstado(EstadoSolicitud.PAGO_TOTAL_PENDIENTE);

            NotificacionEntity notificacion = NotificacionEntity.builder()
                    .descripcion(
                            "Código de solicitud: " + solicitud.getIdSolicitud() + "\n\n" +
                                    "Su pago final ha sido rechazado.\n\n" +
                                    "Motivo del rechazo: " + request.getDescripcion()
                    )
                    .estado(false)
                    .usuario(solicitud.getUsuario())
                    .build();

            notificacionRepository.save(notificacion);
        }

        solicitudRepository.save(solicitud);

        return ApiResponse.success(
                pago.getIdPagoTotal(),
                limiteAlcanzado
                        ? "El pago fue rechazado y la solicitud quedó rechazada definitivamente por alcanzar el límite de intentos."
                        : "Pago final rechazado correctamente. Se notificó al usuario"
        );
    }
}