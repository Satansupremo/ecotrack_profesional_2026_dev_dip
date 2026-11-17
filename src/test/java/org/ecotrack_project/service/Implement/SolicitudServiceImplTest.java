package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.SolicitudRequest;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceImplTest {

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AsignacionCoordinadorRepository asignacionCoordinadorRepository;
    @Mock
    private RecomendacionRepository recomendacionRepository;
    @Mock
    private ProgramacionRepository programacionRepository;
    @Mock
    private DetalleRepository detalleRepository;
    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private ArticuloRecuperadoRepository articuloRecuperadoRepository;
    @Mock
    private PagoInicialRepository pagoInicialRepository;
    @Mock
    private PagoTotalRepository pagoTotalRepository;

    @InjectMocks
    private SolicitudServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarSolicitudAsociaAlUsuarioAutenticadoYLaDejaPendiente() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ana", "n/a", List.of())
        );
        UsuarioEntity usuario = UsuarioEntity.builder()
                .idUsuario(5)
                .username("ana")
                .build();
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario));
        when(solicitudRepository.save(any(SolicitudEntity.class))).thenAnswer(invocation -> {
            SolicitudEntity solicitud = invocation.getArgument(0);
            solicitud.setIdSolicitud(31);
            return solicitud;
        });
        SolicitudRequest request = SolicitudRequest.builder()
                .titulo("Reciclaje")
                .descripcion("Retiro de materiales")
                .ubicacion("Lima")
                .build();

        var response = service.registrarSolicitud(request);

        ArgumentCaptor<SolicitudEntity> captor = ArgumentCaptor.forClass(SolicitudEntity.class);
        verify(solicitudRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(31, response.getData());
        assertEquals(usuario, captor.getValue().getUsuario());
        assertEquals(EstadoSolicitud.PENDIENTE, captor.getValue().getEstado());
        assertEquals(request.getTitulo(), captor.getValue().getTitulo());
    }
}
