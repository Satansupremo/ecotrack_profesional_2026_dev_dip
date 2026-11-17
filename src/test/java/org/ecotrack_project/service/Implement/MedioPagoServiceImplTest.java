package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.MedioPagoRequest;
import org.ecotrack_project.entity.MedioPagoEntity;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedioPagoServiceImplTest {

    @Mock
    private MedioPagoRepository medioPagoRepository;

    @InjectMocks
    private MedioPagoServiceImpl medioPagoService;

    @Test
    void registrarMedioPagoLoGuardaActivo() {
        MedioPagoRequest request = MedioPagoRequest.builder()
                .nombre("Transferencia")
                .descripcion("Cuenta bancaria")
                .numeroCuenta("123456789")
                .build();
        when(medioPagoRepository.save(any(MedioPagoEntity.class))).thenAnswer(invocation -> {
            MedioPagoEntity entity = invocation.getArgument(0);
            entity.setIdMedioPago(3);
            return entity;
        });

        var response = medioPagoService.registrarMedioPago(request);

        ArgumentCaptor<MedioPagoEntity> captor = ArgumentCaptor.forClass(MedioPagoEntity.class);
        verify(medioPagoRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(3, response.getData());
        assertEquals(request.getNombre(), captor.getValue().getNombre());
        assertTrue(captor.getValue().isEstado());
    }

    @Test
    void actualizarMedioPagoInexistenteDevuelveErrorSinGuardar() {
        when(medioPagoRepository.findById(12)).thenReturn(Optional.empty());

        var response = medioPagoService.actualizarMedioPago(12, MedioPagoRequest.builder().build());

        assertFalse(response.isSuccess());
        assertEquals("Medio de pago no encontrado", response.getMessage());
        verify(medioPagoRepository, never()).save(any(MedioPagoEntity.class));
    }
}
