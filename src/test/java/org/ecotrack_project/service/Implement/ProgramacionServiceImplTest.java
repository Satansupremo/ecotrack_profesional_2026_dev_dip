package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.ProgramacionRequest;
import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.security.UsuarioDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgramacionServiceImplTest {

    @Mock
    private ProgramacionRepository programacionRepository;
    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RecomendacionRepository recomendacionRepository;
    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private DetalleRepository detalleRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private ColaRepository colaRepository;
    @Mock
    private LiberacionRecursosService liberacionRecursosService;
    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private FinalizacionProgramacionService finalizacionProgramacionService;

    @InjectMocks
    private ProgramacionServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarProgramacionAsignaJefeActualizaSolicitudYNotifica() {
        UsuarioEntity coordinador = UsuarioEntity.builder()
                .idUsuario(9)
                .username("coord")
                .rol(Rol.COORDINADOR)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new UsuarioDetails(coordinador), null, List.of())
        );
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .idSolicitud(4)
                .estado(EstadoSolicitud.PAGO_TOTAL_CONFIRMADO)
                .build();
        UsuarioEntity jefe = UsuarioEntity.builder()
                .idUsuario(12)
                .rol(Rol.JEFE_UNIDAD)
                .build();
        LocalDate fecha = LocalDate.of(2026, 11, 16);
        when(solicitudRepository.findByIdForUpdate(4)).thenReturn(Optional.of(solicitud));
        when(programacionRepository.existsBySolicitud_IdSolicitud(4)).thenReturn(false);
        when(usuarioRepository.findByRolAndEstadoTrue(Rol.JEFE_UNIDAD)).thenReturn(List.of(jefe));
        when(programacionRepository.findFirstByJefeUnidad_IdUsuarioOrderByFechaRegistroDescIdProgramacionDesc(12))
                .thenReturn(Optional.empty());
        when(programacionRepository.save(any(ProgramacionEntity.class))).thenAnswer(invocation -> {
            ProgramacionEntity programacion = invocation.getArgument(0);
            programacion.setIdProgramacion(18);
            return programacion;
        });
        ProgramacionRequest request = ProgramacionRequest.builder()
                .idSolicitud(4)
                .fechaEjecucion(fecha)
                .build();

        var response = service.registrarProgramacion(request);

        ArgumentCaptor<ProgramacionEntity> captor = ArgumentCaptor.forClass(ProgramacionEntity.class);
        verify(programacionRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(18, response.getData());
        assertEquals(jefe, captor.getValue().getJefeUnidad());
        assertEquals(EstadoProgramacion.ASIGNADO, captor.getValue().getEstado());
        assertEquals(EstadoSolicitud.PROGRAMADO, solicitud.getEstado());
        verify(notificacionRepository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void registrarProgramacionRechazaFechaDeFinDeSemanaSinConsultarRepositorios() {
        ProgramacionRequest request = ProgramacionRequest.builder()
                .idSolicitud(4)
                .fechaEjecucion(LocalDate.of(2026, 11, 15))
                .build();

        var response = service.registrarProgramacion(request);

        assertFalse(response.isSuccess());
        assertEquals(
                "La fecha de ejecución debe corresponder a un día laborable de lunes a viernes",
                response.getMessage()
        );
        verify(solicitudRepository, never()).findByIdForUpdate(anyInt());
    }
}
