package org.ecotrack_project.service.Implement;

import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.enums.EstadoCarga;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.repository.CargaRepository;
import org.ecotrack_project.repository.ColaRepository;
import org.ecotrack_project.repository.ProgramacionRepository;
import org.ecotrack_project.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalizacionProgramacionServiceTest {

    @Mock
    private ProgramacionRepository programacionRepository;
    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private ColaRepository colaRepository;
    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private LiberacionRecursosService liberacionRecursosService;

    @InjectMocks
    private FinalizacionProgramacionService service;

    @Test
    void verificarYFinalizarCompletaSolicitudCuandoTodasLasSalidasFueronAtendidas() {
        SolicitudEntity solicitud = SolicitudEntity.builder()
                .estado(EstadoSolicitud.PROGRAMADO)
                .build();
        ProgramacionEntity programacion = ProgramacionEntity.builder()
                .idProgramacion(15)
                .estado(EstadoProgramacion.POR_FINALIZAR)
                .solicitud(solicitud)
                .build();
        when(programacionRepository.findByIdForUpdate(15)).thenReturn(Optional.of(programacion));
        when(colaRepository.countByDetalle_Programacion_IdProgramacion(15)).thenReturn(2L);
        when(colaRepository.countByDetalle_Programacion_IdProgramacionAndEstadoTrue(15)).thenReturn(0L);
        when(cargaRepository.findByProgramacion_IdProgramacionAndEstado(15, EstadoCarga.REGISTRADA))
                .thenReturn(List.of(new org.ecotrack_project.entity.CargaEntity(),
                        new org.ecotrack_project.entity.CargaEntity()));

        boolean finalized = service.verificarYFinalizar(15);

        assertTrue(finalized);
        assertEquals(EstadoProgramacion.FINALIZADO, programacion.getEstado());
        assertEquals(EstadoSolicitud.FINALIZADO, solicitud.getEstado());
        verify(solicitudRepository).save(solicitud);
        verify(programacionRepository).save(programacion);
        verify(liberacionRecursosService).liberarRecursos(15);
    }

    @Test
    void verificarYFinalizarNoCambiaEstadoSiQuedanSalidasPendientes() {
        ProgramacionEntity programacion = ProgramacionEntity.builder()
                .idProgramacion(15)
                .estado(EstadoProgramacion.POR_FINALIZAR)
                .build();
        when(programacionRepository.findByIdForUpdate(15)).thenReturn(Optional.of(programacion));
        when(colaRepository.countByDetalle_Programacion_IdProgramacion(15)).thenReturn(2L);
        when(colaRepository.countByDetalle_Programacion_IdProgramacionAndEstadoTrue(15)).thenReturn(1L);
        when(cargaRepository.findByProgramacion_IdProgramacionAndEstado(15, EstadoCarga.REGISTRADA))
                .thenReturn(List.of(new org.ecotrack_project.entity.CargaEntity()));

        boolean finalized = service.verificarYFinalizar(15);

        assertFalse(finalized);
        assertEquals(EstadoProgramacion.POR_FINALIZAR, programacion.getEstado());
        verify(programacionRepository, never()).save(programacion);
        verify(liberacionRecursosService, never()).liberarRecursos(15);
    }
}
