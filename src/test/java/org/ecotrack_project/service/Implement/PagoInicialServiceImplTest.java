package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.PagoInicialRequest;
import org.ecotrack_project.entity.AsignacionInicialEntity;
import org.ecotrack_project.entity.MedioPagoEntity;
import org.ecotrack_project.entity.PagoInicialEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.repository.AsignacionInicialRepository;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.ecotrack_project.repository.NotificacionRepository;
import org.ecotrack_project.repository.PagoInicialRepository;
import org.ecotrack_project.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoInicialServiceImplTest {

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private AsignacionInicialRepository asignacionInicialRepository;
    @Mock
    private PagoInicialRepository pagoInicialRepository;
    @Mock
    private MedioPagoRepository medioPagoRepository;
    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private PagoInicialServiceImpl service;

    @Test
    void registrarPagoInicialUsaElMontoAsignadoYAvanzaElEstado() {
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .idSolicitud(4)
                .estado(EstadoSolicitud.PAGO_INICIAL_PENDIENTE)
                .build();
        AsignacionInicialEntity asignacion = AsignacionInicialEntity.builder()
                .montoAsignadoInicial(new BigDecimal("25.00"))
                .build();
        MedioPagoEntity medioPago = MedioPagoEntity.builder()
                .idMedioPago(3)
                .nombre("Transferencia")
                .estado(true)
                .build();
        PagoInicialRequest request = new PagoInicialRequest();
        request.setIdMedioPago(3);
        request.setMonto(new BigDecimal("999.00"));
        request.setFechaOperacion(LocalDate.of(2026, 11, 1));
        request.setHoraOperacion(LocalTime.of(10, 30));
        request.setNumeroOperacion("OP-123");
        when(solicitudRepository.findById(4)).thenReturn(Optional.of(solicitud));
        when(asignacionInicialRepository.findBySolicitud_IdSolicitud(4)).thenReturn(Optional.of(asignacion));
        when(medioPagoRepository.findById(3)).thenReturn(Optional.of(medioPago));
        when(pagoInicialRepository.save(any(PagoInicialEntity.class))).thenAnswer(invocation -> {
            PagoInicialEntity pago = invocation.getArgument(0);
            pago.setIdPagoInicial(17);
            return pago;
        });

        var response = service.registrarPagoInicial(4, request);

        ArgumentCaptor<PagoInicialEntity> captor = ArgumentCaptor.forClass(PagoInicialEntity.class);
        verify(pagoInicialRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(17, response.getData());
        assertEquals(new BigDecimal("25.00"), captor.getValue().getMonto());
        assertEquals(medioPago, captor.getValue().getMedioPago());
        assertEquals(EstadoSolicitud.PAGO_INICIAL_EFECTUADO, solicitud.getEstado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void registrarPagoInicialRechazaSolicitudEnEstadoIncorrecto() {
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .estado(EstadoSolicitud.PENDIENTE)
                .build();
        when(solicitudRepository.findById(4)).thenReturn(Optional.of(solicitud));

        var response = service.registrarPagoInicial(4, new PagoInicialRequest());

        assertFalse(response.isSuccess());
        assertEquals("La solicitud no está pendiente de registrar el pago inicial", response.getMessage());
        verify(asignacionInicialRepository, never()).findBySolicitud_IdSolicitud(4);
        verify(pagoInicialRepository, never()).save(any(PagoInicialEntity.class));
    }
}
