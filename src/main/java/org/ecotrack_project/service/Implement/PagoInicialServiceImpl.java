package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.PagoInicialRequest;
import org.ecotrack_project.dto.request.RechazoPagoInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.PagoInicialDetalleResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoPagoInicial;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.service.PagoInicialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PagoInicialServiceImpl implements PagoInicialService {

    private final SolicitudRepository solicitudRepository;
    private final AsignacionInicialRepository asignacionInicialRepository;
    private final PagoInicialRepository pagoInicialRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    @Transactional
    public ApiResponse<Integer> registrarPagoInicial(int idSolicitud, PagoInicialRequest request) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_INICIAL_PENDIENTE) {
            return ApiResponse.error("La solicitud no está pendiente de registrar el pago inicial");
        }

        Optional<AsignacionInicialEntity> asignacionOptional =
                asignacionInicialRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error("No se encontró la asignación inicial de esta solicitud");
        }

        AsignacionInicialEntity asignacion = asignacionOptional.get();

        Optional<MedioPagoEntity> medioPagoOptional =
                medioPagoRepository.findById(request.getIdMedioPago());

        if (medioPagoOptional.isEmpty() || !medioPagoOptional.get().isEstado()) {
            return ApiResponse.error("Medio de pago no encontrado o no disponible");
        }

        PagoInicialEntity pago = PagoInicialEntity.builder()
                .monto(asignacion.getMontoAsignadoInicial())
                .fechaOperacion(request.getFechaOperacion())
                .horaOperacion(request.getHoraOperacion())
                .numeroOperacion(request.getNumeroOperacion())
                .estado(EstadoPagoInicial.PENDIENTE)
                .medioPago(medioPagoOptional.get())
                .solicitud(solicitud)
                .build();

        PagoInicialEntity guardado = pagoInicialRepository.save(pago);

        solicitud.setEstado(EstadoSolicitud.PAGO_INICIAL_EFECTUADO);
        solicitudRepository.save(solicitud);

        return ApiResponse.success(
                guardado.getIdPagoInicial(),
                "Pago inicial registrado correctamente. Pendiente de confirmación"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagoInicialDetalleResponse> obtenerDetallePagoInicial(int idSolicitud) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_INICIAL_EFECTUADO) {
            return ApiResponse.error(
                    "La solicitud no se encuentra pendiente de revisión del pago inicial"
            );
        }

        Optional<AsignacionInicialEntity> asignacionOptional =
                asignacionInicialRepository.findBySolicitud_IdSolicitud(idSolicitud);

        if (asignacionOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró la asignación inicial de esta solicitud"
            );
        }

        Optional<PagoInicialEntity> pagoOptional =
                pagoInicialRepository.findBySolicitud_IdSolicitudAndEstado(
                        idSolicitud,
                        EstadoPagoInicial.PENDIENTE
                );

        if (pagoOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró un pago inicial pendiente de revisión"
            );
        }

        AsignacionInicialEntity asignacion = asignacionOptional.get();
        PagoInicialEntity pago = pagoOptional.get();

        PagoInicialDetalleResponse response = PagoInicialDetalleResponse.builder()
                .idSolicitud(solicitud.getIdSolicitud())
                .tituloSolicitud(solicitud.getTitulo())
                .fechaRegistroSolicitud(solicitud.getFechaRegistro())

                .idAsignacionInicial(asignacion.getIdAsignacionInicial())
                .montoAsignadoInicial(asignacion.getMontoAsignadoInicial())
                .fechaRegistroAsignacion(asignacion.getFechaRegistro())

                .idPagoInicial(pago.getIdPagoInicial())
                .montoPago(pago.getMonto())
                .horaOperacion(pago.getHoraOperacion())
                .numeroOperacion(pago.getNumeroOperacion())
                .estadoPago(pago.getEstado().name())
                .fechaOperacion(pago.getFechaOperacion())

                .nombreMedioPago(pago.getMedioPago().getNombre())
                .build();

        return ApiResponse.success(
                response,
                "Detalle del pago inicial obtenido correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> confirmarPagoInicial(int idSolicitud) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_INICIAL_EFECTUADO) {
            return ApiResponse.error(
                    "La solicitud no está pendiente de confirmación del pago inicial"
            );
        }

        Optional<PagoInicialEntity> pagoOptional =
                pagoInicialRepository.findBySolicitud_IdSolicitudAndEstado(
                        idSolicitud,
                        EstadoPagoInicial.PENDIENTE
                );

        if (pagoOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró un pago inicial pendiente de confirmación"
            );
        }

        PagoInicialEntity pago = pagoOptional.get();

        pago.setEstado(EstadoPagoInicial.CONFIRMADO);
        pagoInicialRepository.save(pago);

        solicitud.setEstado(EstadoSolicitud.PAGO_INICIAL_CONFIRMADO);
        solicitudRepository.save(solicitud);

        NotificacionEntity notificacion = NotificacionEntity.builder()
                .descripcion(
                        "Código de solicitud: " + solicitud.getIdSolicitud() + "\n\n" +
                        Initialize.MENSAJE_CONFIRMACION_PAGO_INICIAL
                )
                .estado(false)
                .usuario(solicitud.getUsuario())
                .build();

        notificacionRepository.save(notificacion);

        return ApiResponse.success(
                pago.getIdPagoInicial(),
                "Pago inicial confirmado correctamente. Se notificó al usuario"
        );
    }



    @Override
    @Transactional
    public ApiResponse<Integer> rechazarPagoInicial(
            int idSolicitud,
            RechazoPagoInicialRequest request) {

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findById(idSolicitud);

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_INICIAL_EFECTUADO) {
            return ApiResponse.error(
                    "La solicitud no está pendiente de revisión del pago inicial"
            );
        }

        Optional<PagoInicialEntity> pagoOptional =
                pagoInicialRepository.findBySolicitud_IdSolicitudAndEstado(
                        idSolicitud,
                        EstadoPagoInicial.PENDIENTE
                );

        if (pagoOptional.isEmpty()) {
            return ApiResponse.error(
                    "No se encontró un pago inicial pendiente de rechazo"
            );
        }

        PagoInicialEntity pago = pagoOptional.get();

        pago.setEstado(EstadoPagoInicial.RECHAZADO);
        pagoInicialRepository.saveAndFlush(pago);

        long cantidadIntentos =
                pagoInicialRepository.countBySolicitud_IdSolicitud(idSolicitud);

        boolean limiteAlcanzado = cantidadIntentos >= Initialize.CANTIDAD_INTENTO_PAGO_INICIAL;

        if (limiteAlcanzado) {

            solicitud.setEstado(EstadoSolicitud.RECHAZADO);

            NotificacionEntity notificacion = NotificacionEntity.builder()
                    .descripcion(
                            "Código de solicitud: " + solicitud.getIdSolicitud() + "\n\n" +
                            Initialize.MENSAJE_RECHAZO_PAGO_INICIAL
                                    + request.getDescripcion()
                    )
                    .estado(false)
                    .usuario(solicitud.getUsuario())
                    .build();

            notificacionRepository.save(notificacion);

        } else {

            solicitud.setEstado(EstadoSolicitud.PAGO_INICIAL_PENDIENTE);

            NotificacionEntity notificacion = NotificacionEntity.builder()
                    .descripcion(request.getDescripcion())
                    .estado(false)
                    .usuario(solicitud.getUsuario())
                    .build();

            notificacionRepository.save(notificacion);
        }

        solicitudRepository.save(solicitud);

        return ApiResponse.success(
                pago.getIdPagoInicial(),
                limiteAlcanzado
                        ? "El pago fue rechazado y la solicitud quedó rechazada definitivamente por alcanzar el límite de intentos."
                        : "Pago inicial rechazado correctamente. Se notificó al usuario"
        );
    }


}
