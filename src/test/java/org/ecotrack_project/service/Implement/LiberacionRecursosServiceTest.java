package org.ecotrack_project.service.Implement;

import org.ecotrack_project.entity.DetalleEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.repository.DetalleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiberacionRecursosServiceTest {

    @Mock
    private DetalleRepository detalleRepository;

    @InjectMocks
    private LiberacionRecursosService service;

    @Test
    void liberarRecursosDevuelveTransportistasYVehiculosADisponibles() {
        UsuarioEntity transportista = UsuarioEntity.builder()
                .situacion(SituacionUsuario.ASIGNADO)
                .build();
        VehiculoEntity vehiculo = VehiculoEntity.builder()
                .situacion(SituacionVehiculo.ASIGNADO)
                .build();
        DetalleEntity detalle = DetalleEntity.builder()
                .transportista(transportista)
                .vehiculo(vehiculo)
                .build();
        when(detalleRepository.findByProgramacion_IdProgramacionOrderByIdDetalleAsc(12))
                .thenReturn(List.of(detalle));

        service.liberarRecursos(12);

        assertEquals(SituacionUsuario.DISPONIBLE, transportista.getSituacion());
        assertEquals(SituacionVehiculo.DISPONIBLE, vehiculo.getSituacion());
        verify(detalleRepository).findByProgramacion_IdProgramacionOrderByIdDetalleAsc(12);
    }
}
