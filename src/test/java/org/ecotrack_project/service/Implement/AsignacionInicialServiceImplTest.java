package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.AsignacionInicialRequest;
import org.ecotrack_project.entity.AsignacionInicialEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.AsignacionInicialRepository;
import org.ecotrack_project.repository.NotificacionRepository;
import org.ecotrack_project.repository.SolicitudRepository;
import org.ecotrack_project.repository.UsuarioRepository;
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
class AsignacionInicialServiceImplTest {

    @Mock
    private AsignacionInicialRepository asignacionInicialRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private AsignacionInicialServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarAsignacionInicialCreaAsignacionActualizaSolicitudYNotifica() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("coord", "n/a", List.of())
        );
        UsuarioEntity coordinador = UsuarioEntity.builder()
                .idUsuario(9)
                .username("coord")
                .rol(Rol.COORDINADOR)
                .build();
        UsuarioEntity cliente = UsuarioEntity.builder().idUsuario(2).build();
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .idSolicitud(4)
                .usuario(cliente)
                .estado(EstadoSolicitud.PENDIENTE)
                .build();
        AsignacionInicialRequest request = AsignacionInicialRequest.builder()
                .montoAsignadoInicial(new BigDecimal("25.00"))
                .razon("Traslado para verificación")
                .build();
        when(usuarioRepository.findByUsername("coord")).thenReturn(Optional.of(coordinador));
        when(solicitudRepository.findByIdSolicitudAndEstado(4, EstadoSolicitud.PENDIENTE))
                .thenReturn(Optional.of(solicitud));
        when(asignacionInicialRepository.save(any(AsignacionInicialEntity.class)))
                .thenAnswer(invocation -> {
                    AsignacionInicialEntity asignacion = invocation.getArgument(0);
                    asignacion.setIdAsignacionInicial(15);
                    return asignacion;
                });

        var response = service.registrarAsignacionInicial(4, request);

        ArgumentCaptor<AsignacionInicialEntity> captor =
                ArgumentCaptor.forClass(AsignacionInicialEntity.class);
        verify(asignacionInicialRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(15, response.getData());
        assertEquals(new BigDecimal("25.00"), captor.getValue().getMontoAsignadoInicial());
        assertEquals(EstadoSolicitud.PAGO_INICIAL_PENDIENTE, solicitud.getEstado());
        verify(solicitudRepository).save(solicitud);
        verify(notificacionRepository).save(any());
    }
}
