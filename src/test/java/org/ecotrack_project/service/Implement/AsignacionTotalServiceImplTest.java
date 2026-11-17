package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.AsignacionTotalRequest;
import org.ecotrack_project.dto.request.RecomendacionRequest;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoPagoInicial;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionTotalServiceImplTest {

    @Mock
    private AsignacionTotalRepository asignacionTotalRepository;
    @Mock
    private RecomendacionRepository recomendacionRepository;
    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PagoInicialRepository pagoInicialRepository;
    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private AsignacionCoordinadorRepository asignacionCoordinadorRepository;

    @InjectMocks
    private AsignacionTotalServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarAsignacionTotalDescuentaElPagoInicialYLiberaLaAsignacion() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("coord", "n/a", List.of())
        );
        UsuarioEntity coordinador = UsuarioEntity.builder()
                .idUsuario(9)
                .username("coord")
                .rol(Rol.COORDINADOR)
                .build();
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .idSolicitud(4)
                .estado(EstadoSolicitud.EN_VERIFICACION)
                .build();
        AsignacionCoordinadorEntity asignacionCoordinador =
                AsignacionCoordinadorEntity.builder()
                        .coordinador(coordinador)
                        .solicitud(solicitud)
                        .build();
        PagoInicialEntity pagoInicial = PagoInicialEntity.builder()
                .monto(new BigDecimal("20.00"))
                .estado(EstadoPagoInicial.CONFIRMADO)
                .build();
        when(usuarioRepository.findByUsernameForUpdate("coord")).thenReturn(Optional.of(coordinador));
        when(solicitudRepository.findByIdForUpdate(4)).thenReturn(Optional.of(solicitud));
        when(asignacionCoordinadorRepository.findBySolicitud_IdSolicitud(4))
                .thenReturn(Optional.of(asignacionCoordinador));
        when(asignacionTotalRepository.findBySolicitud_IdSolicitud(4)).thenReturn(Optional.empty());
        when(pagoInicialRepository.findBySolicitud_IdSolicitudAndEstado(4, EstadoPagoInicial.CONFIRMADO))
                .thenReturn(Optional.of(pagoInicial));
        when(asignacionTotalRepository.save(any(AsignacionTotalEntity.class)))
                .thenAnswer(invocation -> {
                    AsignacionTotalEntity asignacion = invocation.getArgument(0);
                    asignacion.setIdAsignacionTotal(21);
                    return asignacion;
                });
        AsignacionTotalRequest asignacionRequest = AsignacionTotalRequest.builder()
                .montoAsignadoTotal(new BigDecimal("100.00"))
                .razon("Servicio completo")
                .build();
        RecomendacionRequest recomendacionRequest = RecomendacionRequest.builder()
                .cantidadUnidades(2)
                .razonCantidadUnidades("Volumen estimado")
                .equipoSeguridad("Guantes")
                .otrasRecomendaciones("Manipular con cuidado")
                .build();

        var response = service.registrarAsignacionTotal(4, asignacionRequest, recomendacionRequest);

        ArgumentCaptor<AsignacionTotalEntity> captor =
                ArgumentCaptor.forClass(AsignacionTotalEntity.class);
        verify(asignacionTotalRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(21, response.getData());
        assertEquals(new BigDecimal("20.00"), captor.getValue().getMontoDescontado());
        assertEquals(new BigDecimal("80.00"), captor.getValue().getMontoPorCobrar());
        assertEquals(EstadoSolicitud.PAGO_TOTAL_PENDIENTE, solicitud.getEstado());
        verify(recomendacionRepository).save(any(RecomendacionEntity.class));
        verify(asignacionCoordinadorRepository).delete(asignacionCoordinador);
    }
}
