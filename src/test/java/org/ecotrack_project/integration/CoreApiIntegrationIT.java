package org.ecotrack_project.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.request.MedioPagoRequest;
import org.ecotrack_project.dto.request.SuperUsuarioRequest;
import org.ecotrack_project.dto.request.UsuarioRequest;
import org.ecotrack_project.dto.request.VehiculoRequest;
import org.ecotrack_project.entity.MedioPagoEntity;
import org.ecotrack_project.entity.NotificacionEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.repository.AsignacionCoordinadorRepository;
import org.ecotrack_project.repository.AsignacionInicialRepository;
import org.ecotrack_project.repository.NotificacionRepository;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.ecotrack_project.repository.SolicitudRepository;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.repository.VehiculoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Transactional
class CoreApiIntegrationIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private MedioPagoRepository medioPagoRepository;
    @Autowired
    private SolicitudRepository solicitudRepository;
    @Autowired
    private NotificacionRepository notificacionRepository;
    @Autowired
    private AsignacionCoordinadorRepository asignacionCoordinadorRepository;
    @Autowired
    private AsignacionInicialRepository asignacionInicialRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrarCuentaEIniciarSesionPersisteYAutenticaAlUsuario() throws Exception {
        String username = unique("cliente");
        UsuarioRequest request = UsuarioRequest.builder()
                .nombre("Ana")
                .apellido("Perez")
                .dni("12345678")
                .correo(unique("ana") + "@example.com")
                .celular("987654321")
                .username(username)
                .password("ClaveSegura123")
                .build();

        mockMvc.perform(post("/api/usuario/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        UsuarioEntity creado = usuarioRepository.findByUsername(username).orElseThrow();
        assertTrue(passwordEncoder.matches("ClaveSegura123", creado.getPassword()));

        mockMvc.perform(post("/api/usuario/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                LoginRequest.builder()
                                        .username(username)
                                        .password("ClaveSegura123")
                                        .build()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    void endpointsDeUsuariosAdministradoresCubrenConsultaCreacionEdicionYEstado() throws Exception {
        String username = unique("staff");
        SuperUsuarioRequest request = SuperUsuarioRequest.builder()
                .nombre("Coordinador")
                .apellido("Prueba")
                .correo(unique("staff") + "@example.com")
                .celular("911111111")
                .username(username)
                .password("ClaveSegura123")
                .dni("87654321")
                .rol(Rol.ADMINISTRADOR)
                .build();
        var admin = user("admin").roles("ADMINISTRADOR");

        mockMvc.perform(post("/api/usuario/crear-superusuario")
                        .with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        UsuarioEntity creado = usuarioRepository.findByUsername(username).orElseThrow();
        mockMvc.perform(get("/api/usuario/total-paginas").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/usuario/listar/0").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/usuario/buscar/{id}", creado.getIdUsuario()).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username));

        SuperUsuarioRequest actualizacion = SuperUsuarioRequest.builder()
                .nombre("Coordinador actualizado")
                .apellido("Prueba")
                .correo(creado.getCorreo())
                .celular(creado.getCelular())
                .username(username)
                .password("ClaveSegura123")
                .dni(creado.getDni())
                .rol(Rol.COORDINADOR)
                .build();
        mockMvc.perform(put("/api/usuario/actualizar/{id}", creado.getIdUsuario())
                        .with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(patch("/api/usuario/cambiar-estado/{id}?nuevoEstado=false", creado.getIdUsuario())
                        .with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        UsuarioEntity actualizado = usuarioRepository.findById(creado.getIdUsuario()).orElseThrow();
        assertEquals("Coordinador actualizado", actualizado.getNombre());
        assertEquals(Rol.COORDINADOR, actualizado.getRol());
        assertFalse(actualizado.isEstado());
    }

    @Test
    void endpointsAdministrativosPersistenVehiculosYMediosDePago() throws Exception {
        VehiculoRequest vehiculo = VehiculoRequest.builder()
                .placa(unique("PLACA"))
                .marca("Eco")
                .modelo("V1")
                .capacidadLibras(new java.math.BigDecimal("1200.00"))
                .tipo(TipoVehiculo.VOLQUETE)
                .build();

        mockMvc.perform(post("/api/vehiculo/registrar")
                        .with(user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(vehiculo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertTrue(vehiculoRepository.findAll().stream()
                .anyMatch(guardado -> guardado.getPlaca().equals(vehiculo.getPlaca())));

        MedioPagoRequest medioPago = MedioPagoRequest.builder()
                .nombre(unique("Transferencia"))
                .descripcion("Cuenta de prueba")
                .numeroCuenta("123456789")
                .build();

        mockMvc.perform(post("/api/medio-pago/registrar")
                        .with(user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(medioPago)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void endpointsDeVehiculosCubrenLecturaActualizacionYPaginacion() throws Exception {
        String placa = unique("PLACA");
        var admin = user("admin").roles("ADMINISTRADOR");
        mockMvc.perform(post("/api/vehiculo/registrar")
                        .with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(VehiculoRequest.builder()
                                .placa(placa)
                                .marca("Eco")
                                .modelo("V1")
                                .capacidadLibras(new java.math.BigDecimal("1000.00"))
                                .tipo(TipoVehiculo.VOLQUETE)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        var vehiculo = vehiculoRepository.findAll().stream()
                .filter(item -> item.getPlaca().equals(placa))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/vehiculo/paginas").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/vehiculo/listar?pagina=0").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/vehiculo/buscar/{id}", vehiculo.getIdVehiculo()).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placa").value(placa));
        mockMvc.perform(put("/api/vehiculo/actualizar/{id}", vehiculo.getIdVehiculo())
                        .with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(VehiculoRequest.builder()
                                .placa(placa)
                                .marca("Eco Actualizada")
                                .modelo("V2")
                                .capacidadLibras(new java.math.BigDecimal("1100.00"))
                                .tipo(TipoVehiculo.EXCAVADORA)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(patch("/api/vehiculo/cambiar-estado/{id}?nuevoEstado=false", vehiculo.getIdVehiculo())
                        .with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().isEstado());
    }

    @Test
    void endpointsDeMediosDePagoCubrenLecturaEdicionYEstado() throws Exception {
        var admin = user("admin").roles("ADMINISTRADOR");
        String nombre = unique("Yape");
        mockMvc.perform(post("/api/medio-pago/registrar")
                        .with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(MedioPagoRequest.builder()
                                .nombre(nombre)
                                .descripcion("Billetera digital")
                                .numeroCuenta("987654321")
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
        MedioPagoEntity medioPago = medioPagoRepository.findAll().stream()
                .filter(item -> item.getNombre().equals(nombre))
                .findFirst().orElseThrow();

        mockMvc.perform(get("/api/medio-pago/listar").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/medio-pago/paginas").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/medio-pago/listar-paginado?pagina=0").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/medio-pago/buscar/{id}", medioPago.getIdMedioPago()).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value(nombre));
        mockMvc.perform(put("/api/medio-pago/actualizar/{id}", medioPago.getIdMedioPago())
                        .with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(MedioPagoRequest.builder()
                                .nombre(nombre)
                                .descripcion("Actualizado")
                                .numeroCuenta("123456789")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(patch("/api/medio-pago/cambiar-estado/{id}?nuevoEstado=false",
                        medioPago.getIdMedioPago()).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(medioPagoRepository.findById(medioPago.getIdMedioPago()).orElseThrow().isEstado());
    }

    @Test
    void notificacionSoloSeMarcaLeidaParaElUsuarioAutenticado() throws Exception {
        UsuarioEntity usuario = guardarUsuario(Rol.USUARIO);
        NotificacionEntity notificacion = notificacionRepository.save(
                NotificacionEntity.builder()
                        .descripcion("Aviso de prueba")
                        .estado(false)
                        .usuario(usuario)
                        .build()
        );
        notificacionRepository.save(NotificacionEntity.builder()
                .descripcion("Aviso ya leído")
                .estado(true)
                .usuario(usuario)
                .build());
        var autenticado = user(usuario.getUsername()).roles("USUARIO");

        mockMvc.perform(get("/api/notificacion/total").with(autenticado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));
        mockMvc.perform(get("/api/notificacion/paginas").with(autenticado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/notificacion/mis-notificaciones?pagina=0").with(autenticado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        mockMvc.perform(get("/api/notificacion/no-leidas/total").with(autenticado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        mockMvc.perform(put("/api/notificacion/{id}/leer", notificacion.getIdNotificacion())
                        .with(autenticado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertTrue(notificacionRepository.findById(notificacion.getIdNotificacion())
                .orElseThrow().isEstado());
    }

    @Test
    void coordinadorPuedeTomarSolicitudConfirmadaYQuedaAsignadaEnBaseDeDatos() throws Exception {
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        SolicitudEntity solicitud = guardarSolicitud(EstadoSolicitud.PAGO_INICIAL_CONFIRMADO);

        mockMvc.perform(post("/api/asignacion-coordinador/tomar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoSolicitud.EN_VERIFICACION,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
        assertTrue(asignacionCoordinadorRepository
                .findBySolicitud_IdSolicitud(solicitud.getIdSolicitud()).isPresent());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                                "/api/asignacion-coordinador/cancelar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertEquals(EstadoSolicitud.PAGO_INICIAL_CONFIRMADO,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
    }

    @Test
    void coordinadorPuedeRegistrarAsignacionInicialYPersistirElNuevoEstado() throws Exception {
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        SolicitudEntity solicitud = guardarSolicitud(EstadoSolicitud.PENDIENTE);
        String body = """
                {"montoAsignadoInicial":25.00,"razon":"Traslado de verificación"}
                """;
        var auth = user(coordinador.getUsername()).roles("COORDINADOR");

        mockMvc.perform(post("/api/asignacion-inicial/validar/{id}", solicitud.getIdSolicitud())
                        .with(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/api/asignacion-inicial/registrar/{id}", solicitud.getIdSolicitud())
                        .with(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoSolicitud.PAGO_INICIAL_PENDIENTE,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
        assertTrue(asignacionInicialRepository
                .findBySolicitud_IdSolicitud(solicitud.getIdSolicitud()).isPresent());
        mockMvc.perform(get("/api/asignacion-inicial/buscar/{id}", solicitud.getIdSolicitud())
                        .with(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.montoAsignadoInicial").value(25.0));
    }

    private UsuarioEntity guardarUsuario(Rol rol) {
        String username = unique(rol.name().toLowerCase());
        SituacionUsuario situacion = rol == Rol.USUARIO
                ? SituacionUsuario.NINGUNO
                : SituacionUsuario.DISPONIBLE;
        return usuarioRepository.save(UsuarioEntity.builder()
                .nombre("Usuario")
                .apellido("Integracion")
                .dni(unique("dni"))
                .correo(unique("mail") + "@example.com")
                .celular("900000001")
                .username(username)
                .password(passwordEncoder.encode("Password123"))
                .estado(true)
                .rol(rol)
                .situacion(situacion)
                .build());
    }

    private SolicitudEntity guardarSolicitud(EstadoSolicitud estado) {
        return solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo("Solicitud de integración")
                .descripcion("Solicitud creada para pruebas de integración")
                .ubicacion("Lima")
                .estado(estado)
                .usuario(guardarUsuario(Rol.USUARIO))
                .build());
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
