package org.ecotrack_project.service.Implement;

import org.ecotrack_project.entity.NotificacionEntity;
import org.ecotrack_project.repository.NotificacionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void marcarComoLeidaActualizaNotificacionDelUsuarioAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ana", "n/a", List.of())
        );
        NotificacionEntity notificacion = NotificacionEntity.builder()
                .idNotificacion(5)
                .descripcion("Solicitud recibida")
                .estado(false)
                .build();
        when(notificacionRepository.findByIdNotificacionAndUsuario_Username(5, "ana"))
                .thenReturn(Optional.of(notificacion));

        var response = notificacionService.marcarComoLeida(5);

        assertTrue(response.isSuccess());
        assertEquals(5, response.getData());
        assertTrue(notificacion.isEstado());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    void marcarComoLeidaNoEncuentraNotificacionDeOtroUsuario() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ana", "n/a", List.of())
        );
        when(notificacionRepository.findByIdNotificacionAndUsuario_Username(5, "ana"))
                .thenReturn(Optional.empty());

        var response = notificacionService.marcarComoLeida(5);

        assertFalse(response.isSuccess());
        assertEquals("Notificación no encontrada", response.getMessage());
        verify(notificacionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
