package org.ecotrack_project.service.Implement;

import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.entity.ReservaUnidadEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.enums.TipoVehiculo;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaUnidadServiceImplTest {

    @Mock
    private ReservaUnidadRepository reservaUnidadRepository;
    @Mock
    private ProgramacionRepository programacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private DetalleRepository detalleRepository;

    @InjectMocks
    private ReservaUnidadServiceImpl service;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reservarUnidadEmergenciaReservaVehiculoYTransportistaDisponibles() {
        UsuarioEntity administrador = UsuarioEntity.builder()
                .idUsuario(1)
                .rol(Rol.ADMINISTRADOR)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new UsuarioDetails(administrador), null, List.of()
                )
        );
        ProgramacionEntity programacion = ProgramacionEntity.builder()
                .idProgramacion(10)
                .estado(EstadoProgramacion.EJECUTANDO)
                .build();
        VehiculoEntity vehiculo = VehiculoEntity.builder()
                .idVehiculo(20)
                .placa("ABC-123")
                .marca("Eco")
                .modelo("E1")
                .tipo(TipoVehiculo.VOLQUETE)
                .situacion(SituacionVehiculo.DISPONIBLE)
                .build();
        UsuarioEntity transportista = UsuarioEntity.builder()
                .idUsuario(30)
                .nombre("Luis")
                .apellido("Rojas")
                .celular("987654321")
                .situacion(SituacionUsuario.DISPONIBLE)
                .build();
        when(programacionRepository.findByIdForUpdate(10)).thenReturn(Optional.of(programacion));
        when(reservaUnidadRepository.findByFechaExpiracionLessThanEqual(any()))
                .thenReturn(List.of());
        when(vehiculoRepository.buscarVehiculosDisponiblesConBloqueo(
                TipoVehiculo.VOLQUETE, SituacionVehiculo.DISPONIBLE
        )).thenReturn(List.of(vehiculo));
        when(usuarioRepository.buscarTransportistasDisponiblesConBloqueo(
                Rol.TRANSPORTISTA, SituacionUsuario.DISPONIBLE
        )).thenReturn(List.of(transportista));
        when(reservaUnidadRepository.save(any(ReservaUnidadEntity.class))).thenAnswer(invocation -> {
            ReservaUnidadEntity reserva = invocation.getArgument(0);
            reserva.setIdReserva(40);
            return reserva;
        });

        var response = service.reservarUnidadEmergencia(10, TipoVehiculo.VOLQUETE);

        ArgumentCaptor<ReservaUnidadEntity> captor = ArgumentCaptor.forClass(ReservaUnidadEntity.class);
        verify(reservaUnidadRepository).save(captor.capture());
        assertTrue(response.isSuccess());
        assertEquals(40, response.getData().getIdReserva());
        assertEquals(vehiculo, captor.getValue().getVehiculo());
        assertEquals(transportista, captor.getValue().getTransportista());
        assertEquals(SituacionVehiculo.RESERVADO, vehiculo.getSituacion());
        assertEquals(SituacionUsuario.RESERVADO, transportista.getSituacion());
    }
}
