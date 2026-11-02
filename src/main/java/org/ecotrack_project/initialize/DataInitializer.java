package org.ecotrack_project.initialize;

import org.ecotrack_project.entity.MedioPagoEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final MedioPagoRepository medioPagoRepository;

    private static final String PASSWORD_PRUEBA = "123456";

    @Override
    public void run(String... args) {
        inicializarUsuarios();
        inicializarVehiculos();
        inicializarMediosPago();
    }

    private void inicializarUsuarios() {
        if (usuarioRepository.count() == 0) {
            String passwordEncriptada = passwordEncoder.encode(PASSWORD_PRUEBA);
            List<UsuarioEntity> todosLosUsuarios = new ArrayList<>();

            todosLosUsuarios.add(crearUsuarioBase("Carlos", "Mendoza", "jefe.unidad@ecotrack.com", "987654321", "jefe1", passwordEncriptada, "11111111", Rol.JEFE_UNIDAD));

            todosLosUsuarios.addAll(List.of(
                    crearUsuarioBase("Juan", "Pérez", "juan.perez@ecotrack.com", "900000001", "transportista1", passwordEncriptada, "22222221", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Pedro", "Gómez", "pedro.gomez@ecotrack.com", "900000002", "transportista2", passwordEncriptada, "22222222", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Luis", "Martínez", "luis.martinez@ecotrack.com", "900000003", "transportista3", passwordEncriptada, "22222223", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Ana", "Rodríguez", "ana.rodriguez@ecotrack.com", "900000004", "transportista4", passwordEncriptada, "22222224", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Miguel", "Silva", "miguel.silva@ecotrack.com", "900000005", "transportista5", passwordEncriptada, "22222225", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Jorge", "López", "jorge.lopez@ecotrack.com", "900000006", "transportista6", passwordEncriptada, "22222226", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Carlos", "Ruiz", "carlos.ruiz@ecotrack.com", "900000007", "transportista7", passwordEncriptada, "22222227", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Sofía", "Castro", "sofia.castro@ecotrack.com", "900000008", "transportista8", passwordEncriptada, "22222228", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Diego", "Morales", "diego.morales@ecotrack.com", "900000009", "transportista9", passwordEncriptada, "22222229", Rol.TRANSPORTISTA),
                    crearUsuarioBase("Andrés", "Villa", "andres.villa@ecotrack.com", "900000010", "transportista10", passwordEncriptada, "22222230", Rol.TRANSPORTISTA)
            ));

            todosLosUsuarios.addAll(generarUsuariosPorRol(passwordEncriptada));

            usuarioRepository.saveAll(todosLosUsuarios);
        }
    }

    private List<UsuarioEntity> generarUsuariosPorRol(String passwordEncriptada) {
        return List.of(
                crearUsuarioBase("Administrador", "General", "admin@ecotrack.com", "911111111", "administrador", passwordEncriptada, "33333331", Rol.ADMINISTRADOR),
                crearUsuarioBase("Coordinador", "Logística", "coordinador@ecotrack.com", "922222222", "coordinador", passwordEncriptada, "33333332", Rol.COORDINADOR),
                crearUsuarioBase("Operario", "Planta", "operario@ecotrack.com", "933333333", "operario", passwordEncriptada, "33333333", Rol.OPERARIO),
                crearUsuarioBase("Usuario", "Estándar", "usuario@ecotrack.com", "944444444", "usuario", passwordEncriptada, "33333334", Rol.USUARIO),
                crearUsuarioBase("Jefe", "Estándar", "jefe2@ecotrack.com", "944444444", "jefe2", passwordEncriptada, "33333335", Rol.JEFE_UNIDAD)
        );
    }

    private void inicializarVehiculos() {
        if (vehiculoRepository.count() == 0) {
            List<VehiculoEntity> todosLosVehiculos = new ArrayList<>();

            todosLosVehiculos.addAll(generarVolquetes());
            todosLosVehiculos.addAll(generarExcavadoras());
            todosLosVehiculos.addAll(generarRetroexcavadoras());
            todosLosVehiculos.addAll(generarCargadoresFrontales());
            todosLosVehiculos.addAll(generarTractoresOrugas());

            vehiculoRepository.saveAll(todosLosVehiculos);
        }
    }

    private List<VehiculoEntity> generarVolquetes() {
        return List.of(
                crearVehiculo("VOL-001", "Volvo", "FMX 460", "35000.00", TipoVehiculo.VOLQUETE),
                crearVehiculo("VOL-002", "Scania", "G440", "35000.00", TipoVehiculo.VOLQUETE),
                crearVehiculo("VOL-003", "Mercedes-Benz", "Actros", "32000.00", TipoVehiculo.VOLQUETE),
                crearVehiculo("VOL-004", "Caterpillar", "745", "40000.00", TipoVehiculo.VOLQUETE),
                crearVehiculo("VOL-005", "Mack", "Granite", "38000.00", TipoVehiculo.VOLQUETE)
        );
    }

    private List<VehiculoEntity> generarExcavadoras() {
        return List.of(
                crearVehiculo("EXC-001", "Caterpillar", "320 GX", "48000.00", TipoVehiculo.EXCAVADORA),
                crearVehiculo("EXC-002", "Komatsu", "PC210LC", "47500.00", TipoVehiculo.EXCAVADORA),
                crearVehiculo("EXC-003", "John Deere", "210G", "46000.00", TipoVehiculo.EXCAVADORA),
                crearVehiculo("EXC-004", "Hitachi", "ZX210", "47000.00", TipoVehiculo.EXCAVADORA),
                crearVehiculo("EXC-005", "Hyundai", "HX220L", "46500.00", TipoVehiculo.EXCAVADORA)
        );
    }

    private List<VehiculoEntity> generarRetroexcavadoras() {
        return List.of(
                crearVehiculo("RET-001", "Caterpillar", "416", "15000.00", TipoVehiculo.RETROEXCAVADORA),
                crearVehiculo("RET-002", "John Deere", "310L", "14800.00", TipoVehiculo.RETROEXCAVADORA),
                crearVehiculo("RET-003", "Case", "580 Super N", "15500.00", TipoVehiculo.RETROEXCAVADORA),
                crearVehiculo("RET-004", "JCB", "3CX", "16000.00", TipoVehiculo.RETROEXCAVADORA),
                crearVehiculo("RET-005", "New Holland", "B95B", "14500.00", TipoVehiculo.RETROEXCAVADORA)
        );
    }

    private List<VehiculoEntity> generarCargadoresFrontales() {
        return List.of(
                crearVehiculo("CRG-001", "Caterpillar", "950M", "25000.00", TipoVehiculo.CARGADOR_FRONTAL),
                crearVehiculo("CRG-002", "Komatsu", "WA380", "24500.00", TipoVehiculo.CARGADOR_FRONTAL),
                crearVehiculo("CRG-003", "Volvo", "L120H", "26000.00", TipoVehiculo.CARGADOR_FRONTAL),
                crearVehiculo("CRG-004", "John Deere", "644K", "25500.00", TipoVehiculo.CARGADOR_FRONTAL),
                crearVehiculo("CRG-005", "Liugong", "856H", "24000.00", TipoVehiculo.CARGADOR_FRONTAL)
        );
    }

    private List<VehiculoEntity> generarTractoresOrugas() {
        return List.of(
                crearVehiculo("TRA-001", "Caterpillar", "D6N", "45000.00", TipoVehiculo.TRACTOR_DE_ORUGAS),
                crearVehiculo("TRA-002", "Komatsu", "D65EX", "46000.00", TipoVehiculo.TRACTOR_DE_ORUGAS),
                crearVehiculo("TRA-003", "John Deere", "850K", "44500.00", TipoVehiculo.TRACTOR_DE_ORUGAS),
                crearVehiculo("TRA-004", "Shantui", "SD16", "43000.00", TipoVehiculo.TRACTOR_DE_ORUGAS),
                crearVehiculo("TRA-005", "Liebherr", "PR 736", "47000.00", TipoVehiculo.TRACTOR_DE_ORUGAS)
        );
    }

    // Métodos constructores genéricos (Helper methods)
    private VehiculoEntity crearVehiculo(String placa, String marca, String modelo, String capacidad, TipoVehiculo tipo) {
        return VehiculoEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .placa(placa)
                .marca(marca)
                .modelo(modelo)
                .capacidadLibras(new BigDecimal(capacidad))
                .tipo(tipo)
                .situacion(SituacionVehiculo.DISPONIBLE)
                .estado(true)
                .build();
    }

    private UsuarioEntity crearUsuarioBase(String nombre, String apellido, String correo, String celular, String username, String password, String dni, Rol rol) {

        SituacionUsuario situacion = rol == Rol.USUARIO ? SituacionUsuario.NINGUNO: SituacionUsuario.DISPONIBLE;
        return UsuarioEntity.builder()
                .nombre(nombre)
                .apellido(apellido)
                .correo(correo)
                .celular(celular)
                .username(username)
                .password(password)
                .dni(dni)
                .estado(true)
                .situacion(situacion)
                .rol(rol)
                .build();
    }

    private void inicializarMediosPago() {
        if (medioPagoRepository.count() == 0) {
            List<MedioPagoEntity> mediosPago = List.of(
                    crearMedioPago("Transferencia Bancaria", "Transferencia electrónica vía BCP / BBVA / Interbank", "19199887766012"),
                    crearMedioPago("Yape", "Pago rápido a través de billetera digital Yape", "987654321"),
                    crearMedioPago("Plin", "Pago rápido a través de billetera digital Plin", "987654321")
            );
            medioPagoRepository.saveAll(mediosPago);
        }
    }

    private MedioPagoEntity crearMedioPago(String nombre, String descripcion, String numeroCuenta) {
        return MedioPagoEntity.builder()
                .nombre(nombre).descripcion(descripcion)
                .numeroCuenta(numeroCuenta)
                .estado(true)
                .build();
    }
}
