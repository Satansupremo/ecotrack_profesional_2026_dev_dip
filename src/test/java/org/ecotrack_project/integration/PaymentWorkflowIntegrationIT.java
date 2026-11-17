package org.ecotrack_project.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.*;
import org.ecotrack_project.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Transactional
class PaymentWorkflowIntegrationIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private SolicitudRepository solicitudRepository;
    @Autowired
    private MedioPagoRepository medioPagoRepository;
    @Autowired
    private AsignacionInicialRepository asignacionInicialRepository;
    @Autowired
    private AsignacionTotalRepository asignacionTotalRepository;
    @Autowired
    private AsignacionCoordinadorRepository asignacionCoordinadorRepository;
    @Autowired
    private PagoInicialRepository pagoInicialRepository;
    @Autowired
    private PagoTotalRepository pagoTotalRepository;
    @Autowired
    private RecomendacionRepository recomendacionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrarYConfirmarPagoInicialActualizaSolicitudYPersistencia() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.PAGO_INICIAL_PENDIENTE);
        MedioPagoEntity medioPago = guardarMedioPago();
        asignacionInicialRepository.save(AsignacionInicialEntity.builder()
                .montoAsignadoInicial(new BigDecimal("25.00"))
                .razon("Verificación")
                .fechaRegistro(LocalDateTime.now())
                .solicitud(solicitud)
                .coordinador(coordinador)
                .build());

        mockMvc.perform(post("/api/pago-inicial/pago/registrar/{id}", solicitud.getIdSolicitud())
                        .with(user(cliente.getUsername()).roles("USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idMedioPago":%d,"monto":25.00,"fechaOperacion":"2026-11-15","horaOperacion":"10:30:00","numeroOperacion":"PI-001"}
                                """.formatted(medioPago.getIdMedioPago())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoSolicitud.PAGO_INICIAL_EFECTUADO,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
        PagoInicialEntity pago = pagoInicialRepository
                .findBySolicitud_IdSolicitudAndEstado(solicitud.getIdSolicitud(), EstadoPagoInicial.PENDIENTE)
                .orElseThrow();

        mockMvc.perform(get("/api/pago-inicial/pie/detalle/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.montoPago").value(25.0));

        mockMvc.perform(put("/api/pago-inicial/pie/confirmar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rechazarPagoInicialDevuelveSolicitudAlEstadoPendiente() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.PAGO_INICIAL_EFECTUADO);
        MedioPagoEntity medioPago = guardarMedioPago();
        PagoInicialEntity pago = pagoInicialRepository.save(PagoInicialEntity.builder()
                .monto(new BigDecimal("25.00"))
                .fechaOperacion(LocalDate.of(2026, 11, 10))
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion("PI-RECHAZO")
                .estado(EstadoPagoInicial.PENDIENTE)
                .medioPago(medioPago)
                .solicitud(solicitud)
                .build());

        mockMvc.perform(put("/api/pago-inicial/pie/rechazar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descripcion":"Comprobante ilegible"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoPagoInicial.RECHAZADO,
                pagoInicialRepository.findById(pago.getIdPagoInicial()).orElseThrow().getEstado());
        assertEquals(EstadoSolicitud.PAGO_INICIAL_PENDIENTE,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
    }

    @Test
    void registrarYConfirmarPagoTotalActualizaSolicitudYPersistencia() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.PAGO_TOTAL_PENDIENTE);
        MedioPagoEntity medioPago = guardarMedioPago();
        asignacionTotalRepository.save(AsignacionTotalEntity.builder()
                .montoAsignadoTotal(new BigDecimal("100.00"))
                .montoDescontado(new BigDecimal("25.00"))
                .montoPorCobrar(new BigDecimal("75.00"))
                .razon("Servicio")
                .fechaRegistro(LocalDateTime.now())
                .solicitud(solicitud)
                .coordinador(coordinador)
                .build());

        mockMvc.perform(post("/api/pago-total/pago/registrar/{id}", solicitud.getIdSolicitud())
                        .with(user(cliente.getUsername()).roles("USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idMedioPago":%d,"monto":75.00,"fechaOperacion":"2026-11-15","horaOperacion":"11:30:00","numeroOperacion":"PT-001"}
                                """.formatted(medioPago.getIdMedioPago())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoSolicitud.PAGO_TOTAL_EFECTUADO,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
        PagoTotalEntity pago = pagoTotalRepository
                .findBySolicitud_IdSolicitudAndEstado(solicitud.getIdSolicitud(), EstadoPagoTotal.PENDIENTE)
                .orElseThrow();

        mockMvc.perform(get("/api/pago-total/ptt/detalle/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.montoPago").value(75.0));

        mockMvc.perform(put("/api/pago-total/ptt/confirmar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rechazarPagoTotalDevuelveSolicitudAlEstadoPendiente() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.PAGO_TOTAL_EFECTUADO);
        MedioPagoEntity medioPago = guardarMedioPago();
        PagoTotalEntity pago = pagoTotalRepository.save(PagoTotalEntity.builder()
                .monto(new BigDecimal("75.00"))
                .fechaOperacion(LocalDate.of(2026, 11, 10))
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion("PT-RECHAZO")
                .estado(EstadoPagoTotal.PENDIENTE)
                .medioPago(medioPago)
                .solicitud(solicitud)
                .build());

        mockMvc.perform(put("/api/pago-total/ptt/rechazar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descripcion":"Operación no coincide"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoPagoTotal.RECHAZADO,
                pagoTotalRepository.findById(pago.getIdPagoTotal()).orElseThrow().getEstado());
        assertEquals(EstadoSolicitud.PAGO_TOTAL_PENDIENTE,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
    }

    @Test
    void asignarPagoTotalDescuentaElPagoInicialYGuardaRecomendacion() throws Exception {
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.EN_VERIFICACION);
        MedioPagoEntity medioPago = guardarMedioPago();
        AsignacionCoordinadorEntity asignacionCoordinador =
                asignacionCoordinadorRepository.save(AsignacionCoordinadorEntity.builder()
                        .solicitud(solicitud)
                        .coordinador(coordinador)
                        .build());
        pagoInicialRepository.save(PagoInicialEntity.builder()
                .monto(new BigDecimal("25.00"))
                .fechaOperacion(LocalDate.of(2026, 11, 10))
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion("PI-BASE")
                .estado(EstadoPagoInicial.CONFIRMADO)
                .medioPago(medioPago)
                .solicitud(solicitud)
                .build());

        mockMvc.perform(get("/api/asignacion-total/verificar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/asignacion-total/registrar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "asignacionTotal":{"montoAsignadoTotal":100.00,"razon":"Costo final"},
                                  "recomendacion":{"cantidadUnidades":2,"razonCantidadUnidades":"Volumen","equipoSeguridad":"Guantes","otrasRecomendaciones":"Manipular con cuidado"}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        AsignacionTotalEntity asignacion = asignacionTotalRepository
                .findBySolicitud_IdSolicitud(solicitud.getIdSolicitud()).orElseThrow();
        assertEquals(new BigDecimal("75.00"), asignacion.getMontoPorCobrar());
        assertTrue(recomendacionRepository.findBySolicitud_IdSolicitud(solicitud.getIdSolicitud()).isPresent());
        assertTrue(asignacionCoordinadorRepository
                .findBySolicitud_IdSolicitud(solicitud.getIdSolicitud()).isEmpty());
        assertEquals(EstadoSolicitud.PAGO_TOTAL_PENDIENTE,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
        mockMvc.perform(get("/api/asignacion-total/buscar/{id}", solicitud.getIdSolicitud())
                        .with(user(coordinador.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.montoPorCobrar").value(75.0));
    }

    private UsuarioEntity guardarUsuario(Rol rol) {
        SituacionUsuario situacion = rol == Rol.USUARIO
                ? SituacionUsuario.NINGUNO
                : SituacionUsuario.DISPONIBLE;
        return usuarioRepository.save(UsuarioEntity.builder()
                .nombre("Usuario")
                .apellido("Integration")
                .dni(unique("dni"))
                .correo(unique("mail") + "@example.com")
                .celular("900000001")
                .username(unique(rol.name().toLowerCase()))
                .password(passwordEncoder.encode("Password123"))
                .estado(true)
                .rol(rol)
                .situacion(situacion)
                .build());
    }

    private SolicitudEntity guardarSolicitud(UsuarioEntity usuario, EstadoSolicitud estado) {
        return solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo("Solicitud de integración")
                .descripcion("Solicitud para validar pago")
                .ubicacion("Lima")
                .estado(estado)
                .usuario(usuario)
                .build());
    }

    private MedioPagoEntity guardarMedioPago() {
        return medioPagoRepository.save(MedioPagoEntity.builder()
                .nombre(unique("Medio"))
                .descripcion("Método de pago de integración")
                .numeroCuenta(unique("cuenta"))
                .estado(true)
                .build());
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
