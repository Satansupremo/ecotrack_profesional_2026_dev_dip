
package org.ecotrack_project.initialize;

import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String PASSWORD_PRUEBA = "123456";

    @Override
    public void run(String... args) {

        if (vehiculoRepository.count() == 0) {

            VehiculoEntity v1 = VehiculoEntity.builder()
                    .fechaRegistro(LocalDateTime.now())
                    .placa("ABC-1234")
                    .marca("Toyota")
                    .modelo("Hilux")
                    .capacidadLibras(new BigDecimal("2200.00"))
                    .tipo(TipoVehiculo.CAMIONETA)
                    .estado(true)
                    .build();

            VehiculoEntity v2 = VehiculoEntity.builder()
                    .fechaRegistro(LocalDateTime.now())
                    .placa("XYZ-5678")
                    .marca("Volvo")
                    .modelo("FH16")
                    .capacidadLibras(new BigDecimal("40000.00"))
                    .tipo(TipoVehiculo.CAMION)
                    .estado(true)
                    .build();

            VehiculoEntity v3 = VehiculoEntity.builder()
                    .fechaRegistro(LocalDateTime.now())
                    .placa("MNO-9012")
                    .marca("Hyundai")
                    .modelo("H100")
                    .capacidadLibras(new BigDecimal("3500.00"))
                    .tipo(TipoVehiculo.MINIVAN)
                    .estado(true)
                    .build();

            vehiculoRepository.saveAll(List.of(v1, v2, v3));
        }

        crearUsuarioSiNoExiste(
                "usuario", "Carlos", "Ramirez", "70000001",
                "carlos.ramirez@ecotrack.test", "987654321", Rol.USUARIO);

        crearUsuarioSiNoExiste(
                "administrador", "Ana", "Torres", "70000002",
                "ana.torres@ecotrack.test", "987654322", Rol.ADMINISTRADOR);

        crearUsuarioSiNoExiste(
                "coordinador", "Luis", "Flores", "70000003",
                "luis.flores@ecotrack.test", "987654323", Rol.COORDINADOR);

        crearUsuarioSiNoExiste(
                "transportista", "Pedro", "Castillo", "70000004",
                "pedro.castillo@ecotrack.test", "987654324", Rol.TRANSPORTISTA);

        crearUsuarioSiNoExiste(
                "operario", "Maria", "Vargas", "70000005",
                "maria.vargas@ecotrack.test", "987654325", Rol.OPERARIO);
    }

    private void crearUsuarioSiNoExiste(
            String username,
            String nombre,
            String apellido,
            String dni,
            String correo,
            String celular,
            Rol rol) {

        if (usuarioRepository.findByUsername(username).isEmpty()) {

            UsuarioEntity usuario = UsuarioEntity.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .correo(correo)
                    .celular(celular)
                    .username(username)
                    .password(passwordEncoder.encode(PASSWORD_PRUEBA))
                    .dni(dni)
                    .estado(true)
                    .rol(rol)
                    .build();

            usuarioRepository.save(usuario);
        }
    }
}
