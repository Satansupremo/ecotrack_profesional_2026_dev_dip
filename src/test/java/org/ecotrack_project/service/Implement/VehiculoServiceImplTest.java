package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.VehiculoRequest;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.repository.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceImplTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private VehiculoServiceImpl vehiculoService;

    @Test
    void registrarVehiculoGuardaLaUnidadDisponibleYActiva() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-123")
                .marca("Eco")
                .modelo("E1")
                .capacidadLibras(new BigDecimal("1200.00"))
                .tipo(TipoVehiculo.VOLQUETE)
                .build();
        when(vehiculoRepository.save(any(VehiculoEntity.class))).thenAnswer(invocation -> {
            VehiculoEntity entity = invocation.getArgument(0);
            entity.setIdVehiculo(7);
            return entity;
        });

        var response = vehiculoService.registrarVehiculo(request);

        ArgumentCaptor<VehiculoEntity> captor = ArgumentCaptor.forClass(VehiculoEntity.class);
        verify(vehiculoRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(7, response.getData());
        assertEquals("ABC-123", captor.getValue().getPlaca());
        assertTrue(captor.getValue().isEstado());
        assertEquals(SituacionVehiculo.DISPONIBLE, captor.getValue().getSituacion());
    }

    @Test
    void actualizarVehiculoInexistenteNoGuardaCambios() {
        when(vehiculoRepository.findById(99)).thenReturn(Optional.empty());

        var response = vehiculoService.actualizarVehiculo(99, VehiculoRequest.builder().build());

        assertFalse(response.isSuccess());
        assertEquals("Vehículo no encontrado", response.getMessage());
        verify(vehiculoRepository, never()).save(any(VehiculoEntity.class));
    }
}
