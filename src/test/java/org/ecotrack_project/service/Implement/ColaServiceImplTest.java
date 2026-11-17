package org.ecotrack_project.service.Implement;

import org.ecotrack_project.entity.ColaEntity;
import org.ecotrack_project.entity.DetalleEntity;
import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.repository.ColaRepository;
import org.ecotrack_project.repository.ProgramacionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColaServiceImplTest {

    @Mock
    private ColaRepository colaRepository;
    @Mock
    private ProgramacionRepository programacionRepository;
    @Mock
    private FinalizacionProgramacionService finalizacionProgramacionService;

    @InjectMocks
    private ColaServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recepcionarSalidaDesactivaLaColaYVerificaFinalizacion() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "operario",
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_OPERARIO"))
                )
        );
        ProgramacionEntity programacion = ProgramacionEntity.builder()
                .idProgramacion(11)
                .estado(EstadoProgramacion.EJECUTANDO)
                .build();
        ColaEntity cola = ColaEntity.builder()
                .idCola(21)
                .estado(true)
                .detalle(DetalleEntity.builder().programacion(programacion).build())
                .build();
        when(colaRepository.findById(21)).thenReturn(Optional.of(cola));
        when(programacionRepository.findByIdForUpdate(11)).thenReturn(Optional.of(programacion));

        var response = service.recepcionarSalida(21);

        assertTrue(response.isSuccess());
        assertEquals(21, response.getData());
        assertFalse(cola.isEstado());
        verify(colaRepository).saveAndFlush(cola);
        verify(finalizacionProgramacionService).verificarYFinalizar(11);
    }
}
