package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.IniciarCargaRequest;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoCarga;
import org.ecotrack_project.enums.EstadoProgramacion;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CargaServiceImplTest {

    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private ColaRepository colaRepository;
    @Mock
    private ProgramacionRepository programacionRepository;
    @Mock
    private LiberacionRecursosService liberacionRecursosService;
    @Mock
    private ArticuloRecuperadoRepository articuloRecuperadoRepository;
    @Mock
    private FinalizacionProgramacionService finalizacionProgramacionService;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CargaServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void iniciarCargaRegistraOperarioPesoYDesactivaSalidaRecibida() {
        UsuarioEntity operario = UsuarioEntity.builder()
                .idUsuario(3)
                .rol(Rol.OPERARIO)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new UsuarioDetails(operario), null, List.of())
        );
        UsuarioEntity transportista = UsuarioEntity.builder().idUsuario(7).build();
        ProgramacionEntity programacion = ProgramacionEntity.builder()
                .idProgramacion(11)
                .estado(EstadoProgramacion.EJECUTANDO)
                .build();
        DetalleEntity detalle = DetalleEntity.builder()
                .programacion(programacion)
                .transportista(transportista)
                .build();
        ColaEntity cola = ColaEntity.builder()
                .idCola(21)
                .estado(true)
                .detalle(detalle)
                .build();
        BigDecimal peso = new BigDecimal("125.50");
        when(colaRepository.findById(21)).thenReturn(Optional.of(cola));
        when(programacionRepository.findByIdForUpdate(11)).thenReturn(Optional.of(programacion));
        when(cargaRepository.findByCola_IdCola(21)).thenReturn(Optional.empty());
        when(cargaRepository.saveAndFlush(any(CargaEntity.class))).thenAnswer(invocation -> {
            CargaEntity carga = invocation.getArgument(0);
            carga.setIdCarga(33);
            return carga;
        });

        var response = service.iniciarCarga(IniciarCargaRequest.builder()
                .idCola(21)
                .pesoLibras(peso)
                .build());

        ArgumentCaptor<CargaEntity> captor = ArgumentCaptor.forClass(CargaEntity.class);
        verify(cargaRepository).saveAndFlush(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(33, response.getData().getIdCarga());
        assertEquals(peso, captor.getValue().getPesoLibras());
        assertEquals(operario, captor.getValue().getOperario());
        assertEquals(EstadoCarga.EN_PROCESO, captor.getValue().getEstado());
        assertFalse(cola.isEstado());
        verify(colaRepository).save(cola);
    }
}
