package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.PagoTotalRequest;
import org.ecotrack_project.entity.AsignacionTotalEntity;
import org.ecotrack_project.entity.MedioPagoEntity;
import org.ecotrack_project.entity.PagoTotalEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.repository.AsignacionTotalRepository;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.ecotrack_project.repository.NotificacionRepository;
import org.ecotrack_project.repository.PagoTotalRepository;
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
class PagoTotalServiceImplTest {

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private AsignacionTotalRepository asignacionTotalRepository;
    @Mock
    private PagoTotalRepository pagoTotalRepository;
    @Mock
    private MedioPagoRepository medioPagoRepository;
    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private PagoTotalServiceImpl service;

    @Test
    void registrarPagoTotalRegistraSaldoPendienteYActualizaSolicitud() {
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .idSolicitud(8)
                .estado(EstadoSolicitud.PAGO_TOTAL_PENDIENTE)
                .build();
        AsignacionTotalEntity asignacion = AsignacionTotalEntity.builder()
                .montoPorCobrar(new BigDecimal("80.00"))
                .build();
        MedioPagoEntity medioPago = MedioPagoEntity.builder()
                .idMedioPago(3)
                .nombre("Transferencia")
                .estado(true)
                .build();
        PagoTotalRequest request = new PagoTotalRequest();
        request.setIdMedioPago(3);
        request.setMonto(new BigDecimal("5.00"));
        request.setFechaOperacion(LocalDate.of(2026, 11, 1));
        request.setHoraOperacion(LocalTime.of(12, 0));
        request.setNumeroOperacion("OP-456");
        when(solicitudRepository.findById(8)).thenReturn(Optional.of(solicitud));
        when(asignacionTotalRepository.findBySolicitud_IdSolicitud(8)).thenReturn(Optional.of(asignacion));
        when(medioPagoRepository.findById(3)).thenReturn(Optional.of(medioPago));
        when(pagoTotalRepository.save(any(PagoTotalEntity.class))).thenAnswer(invocation -> {
            PagoTotalEntity pago = invocation.getArgument(0);
            pago.setIdPagoTotal(28);
            return pago;
        });

        var response = service.registrarPagoTotal(8, request);

        ArgumentCaptor<PagoTotalEntity> captor = ArgumentCaptor.forClass(PagoTotalEntity.class);
        verify(pagoTotalRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(28, response.getData());
        assertEquals(new BigDecimal("80.00"), captor.getValue().getMonto());
        assertEquals(medioPago, captor.getValue().getMedioPago());
        assertEquals(EstadoSolicitud.PAGO_TOTAL_EFECTUADO, solicitud.getEstado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void registrarPagoTotalRechazaMedioDePagoInactivo() {
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .estado(EstadoSolicitud.PAGO_TOTAL_PENDIENTE)
                .build();
        when(solicitudRepository.findById(8)).thenReturn(Optional.of(solicitud));
        when(asignacionTotalRepository.findBySolicitud_IdSolicitud(8))
                .thenReturn(Optional.of(AsignacionTotalEntity.builder().build()));
        when(medioPagoRepository.findById(3))
                .thenReturn(Optional.of(MedioPagoEntity.builder().estado(false).build()));
        PagoTotalRequest request = new PagoTotalRequest();
        request.setIdMedioPago(3);

        var response = service.registrarPagoTotal(8, request);

        assertFalse(response.isSuccess());
        assertEquals("Medio de pago no encontrado o no disponible", response.getMessage());
        verify(pagoTotalRepository, never()).save(any(PagoTotalEntity.class));
    }
}
