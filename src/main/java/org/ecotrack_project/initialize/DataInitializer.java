
package org.ecotrack_project.initialize;

import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (vehiculoRepository.count() == 0) {

            VehiculoEntity v1 = VehiculoEntity.builder()
                    .fechaRegistro(new Date())
                    .placa("ABC-1234")
                    .marca("Toyota")
                    .modelo("Hilux")
                    .capacidadLibras(2200.0f)
                    .estado(true)
                    .build();

            VehiculoEntity v2 = VehiculoEntity.builder()
                    .fechaRegistro(new Date())
                    .placa("XYZ-5678")
                    .marca("Volvo")
                    .modelo("FH16")
                    .capacidadLibras(40000.0f)
                    .estado(true)
                    .build();

            VehiculoEntity v3 = VehiculoEntity.builder()
                    .fechaRegistro(new Date())
                    .placa("MNO-9012")
                    .marca("Hyundai")
                    .modelo("H100")
                    .capacidadLibras(3500.0f)
                    .estado(true)
                    .build();

            vehiculoRepository.saveAll(List.of(v1, v2, v3));
        }

        crearUsuarioSiNoExiste(
                "usuario", "Carlos", "Ramirez", "70000001", Rol.USUARIO);

        crearUsuarioSiNoExiste(
                "admin", "Ana", "Torres", "70000002", Rol.ADMINISTRADOR);

        crearUsuarioSiNoExiste(
                "coordinador", "Luis", "Flores", "70000003", Rol.COORDINADOR);

        crearUsuarioSiNoExiste(
                "transportista", "Pedro", "Castillo", "70000004", Rol.TRANSPORTISTA);

        crearUsuarioSiNoExiste(
                "operario", "Maria", "Vargas", "70000005", Rol.OPERARIO);
    }

    private void crearUsuarioSiNoExiste(String username, String nombre, String apellido, String dni, Rol rol) {

        if (usuarioRepository.findByUsername(username).isEmpty()) {

            UsuarioEntity usuario = UsuarioEntity.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .username(username)
                    .password(passwordEncoder.encode("1234"))
                    .dni(dni)
                    .estado(true)
                    .rol(rol)
                    .build();

            usuarioRepository.save(usuario);
        }
    }
}
