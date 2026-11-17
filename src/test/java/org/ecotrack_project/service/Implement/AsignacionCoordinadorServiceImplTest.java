package org.ecotrack_project.service.Implement;

import org.ecotrack_project.entity.AsignacionCoordinadorEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.AsignacionCoordinadorRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionCoordinadorServiceImplTest {

    @Mock
    private AsignacionCoordinadorRepository asignacionCoordinadorRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AsignacionCoordinadorServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tomarSolicitudAsignaAlCoordinadorYActualizaEstado() {
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
                .estado(EstadoSolicitud.PAGO_INICIAL_CONFIRMADO)
                .build();
        when(usuarioRepository.findByUsernameForUpdate("coord")).thenReturn(Optional.of(coordinador));
        when(asignacionCoordinadorRepository.existsByCoordinador_IdUsuario(9)).thenReturn(false);
        when(solicitudRepository.findByIdForUpdate(4)).thenReturn(Optional.of(solicitud));
        when(asignacionCoordinadorRepository.existsBySolicitud_IdSolicitud(4)).thenReturn(false);
        when(asignacionCoordinadorRepository.save(any(AsignacionCoordinadorEntity.class)))
                .thenAnswer(invocation -> {
                    AsignacionCoordinadorEntity asignacion = invocation.getArgument(0);
                    asignacion.setIdAsignacionCoordinador(11);
                    return asignacion;
                });

        var response = service.tomarSolicitud(4);

        ArgumentCaptor<AsignacionCoordinadorEntity> captor =
                ArgumentCaptor.forClass(AsignacionCoordinadorEntity.class);
        verify(asignacionCoordinadorRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(11, response.getData());
        assertEquals(coordinador, captor.getValue().getCoordinador());
        assertEquals(EstadoSolicitud.EN_VERIFICACION, solicitud.getEstado());
        verify(solicitudRepository).save(solicitud);
    }
}
