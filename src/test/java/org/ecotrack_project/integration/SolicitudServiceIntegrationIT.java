package org.ecotrack_project.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecotrack_project.dto.request.SolicitudRequest;
import org.ecotrack_project.entity.ArticuloRecuperadoEntity;
import org.ecotrack_project.entity.AsignacionCoordinadorEntity;
import org.ecotrack_project.entity.CargaEntity;
import org.ecotrack_project.entity.ColaEntity;
import org.ecotrack_project.entity.DetalleEntity;
import org.ecotrack_project.entity.MedioPagoEntity;
import org.ecotrack_project.entity.PagoInicialEntity;
import org.ecotrack_project.entity.PagoTotalEntity;
import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.entity.RecomendacionEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.EstadoCarga;
import org.ecotrack_project.enums.EstadoPagoInicial;
import org.ecotrack_project.enums.EstadoPagoTotal;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.repository.ArticuloRecuperadoRepository;
import org.ecotrack_project.repository.AsignacionCoordinadorRepository;
import org.ecotrack_project.repository.CargaRepository;
import org.ecotrack_project.repository.ColaRepository;
import org.ecotrack_project.repository.DetalleRepository;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.ecotrack_project.repository.PagoInicialRepository;
import org.ecotrack_project.repository.PagoTotalRepository;
import org.ecotrack_project.repository.ProgramacionRepository;
import org.ecotrack_project.repository.RecomendacionRepository;
import org.ecotrack_project.repository.SolicitudRepository;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.repository.VehiculoRepository;
import org.ecotrack_project.service.Implement.ContratoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Transactional
class SolicitudServiceIntegrationIT {

    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private SolicitudRepository solicitudRepository;
    @Autowired
    private RecomendacionRepository recomendacionRepository;
    @Autowired
    private AsignacionCoordinadorRepository asignacionCoordinadorRepository;
    @Autowired
    private ProgramacionRepository programacionRepository;
    @Autowired
    private DetalleRepository detalleRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private ColaRepository colaRepository;
    @Autowired
    private CargaRepository cargaRepository;
    @Autowired
    private ArticuloRecuperadoRepository articuloRecuperadoRepository;
    @Autowired
    private PagoInicialRepository pagoInicialRepository;
    @Autowired
    private PagoTotalRepository pagoTotalRepository;
    @Autowired
    private MedioPagoRepository medioPagoRepository;
    @Autowired
    private ContratoService contratoService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void endpointsDeSolicitudCubrenRegistroConsultaYFiltrosDeEstado() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        UsuarioEntity coordinadorAsignadoPI = guardarUsuario(Rol.COORDINADOR);
        String tokenCliente = iniciarSesion(cliente);
        String tokenCoordinador = iniciarSesion(coordinador);

        MvcResult registro = mockMvc.perform(post("/api/solicitud/registrar")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(SolicitudRequest.builder()
                                .titulo("Recolección de cartón")
                                .descripcion("Retiro de cartón y papel")
                                .ubicacion("Lima")
                                .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        int idPropia = objectMapper.readTree(registro.getResponse().getContentAsString())
                .path("data").asInt();

        SolicitudEntity solicitudPendienteInicial = guardarSolicitud(cliente,
                EstadoSolicitud.PAGO_INICIAL_EFECTUADO);
        SolicitudEntity solicitudInicialConfirmada = guardarSolicitud(cliente,
                EstadoSolicitud.PAGO_INICIAL_CONFIRMADO);
        SolicitudEntity solicitudEnVerificacion = guardarSolicitud(cliente,
                EstadoSolicitud.EN_VERIFICACION);
        SolicitudEntity solicitudPagoTotalEfectuado = guardarSolicitud(cliente,
                EstadoSolicitud.PAGO_TOTAL_EFECTUADO);
        SolicitudEntity solicitudPagoTotalConfirmado = guardarSolicitud(cliente,
                EstadoSolicitud.PAGO_TOTAL_CONFIRMADO);
        asignacionCoordinadorRepository.save(AsignacionCoordinadorEntity.builder()
                .solicitud(solicitudInicialConfirmada)
                .coordinador(coordinadorAsignadoPI)
                .build());
        asignacionCoordinadorRepository.save(AsignacionCoordinadorEntity.builder()
                .solicitud(solicitudPagoTotalConfirmado)
                .coordinador(coordinador)
                .build());
        guardarRecomendacion(solicitudPagoTotalEfectuado);
        guardarRecomendacion(solicitudPagoTotalConfirmado);

        mockMvc.perform(get("/api/solicitud/paginas")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/solicitud/total")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(6));
        mockMvc.perform(get("/api/solicitud/listar")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(6));
        mockMvc.perform(get("/api/solicitud/buscar/{id}", idPropia)
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.data.fases.length()").value(6));

        mockMvc.perform(get("/api/solicitud/pendientes/total")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/solicitud/pendientes/paginas")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/solicitud/pendientes/listar")
                        .header("Authorization", "Bearer " + tokenCoordinador)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/solicitud/pendientes/buscar/{id}",
                                idPropia)
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE"));

        mockMvc.perform(get("/api/solicitud/pie/total")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/solicitud/pie/paginas")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/solicitud/pie/listar")
                        .header("Authorization", "Bearer " + tokenCoordinador)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/solicitud/pi-confirmadas/total")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));
        mockMvc.perform(get("/api/solicitud/pi-confirmadas/paginas")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/solicitud/pi-confirmadas/listar")
                        .header("Authorization", "Bearer " + tokenCoordinador)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        mockMvc.perform(get("/api/solicitud/pi-confirmadas/buscar/{id}",
                                solicitudInicialConfirmada.getIdSolicitud())
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("PAGO_INICIAL_CONFIRMADO"))
                .andExpect(jsonPath("$.data.asignadaAlCoordinadorActual").value(false));
        assertEquals(EstadoSolicitud.EN_VERIFICACION,
                solicitudRepository.findById(solicitudEnVerificacion.getIdSolicitud())
                        .orElseThrow().getEstado());

        mockMvc.perform(get("/api/solicitud/pago-total/total")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));
        mockMvc.perform(get("/api/solicitud/pago-total/paginas")
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/solicitud/pago-total/listar")
                        .header("Authorization", "Bearer " + tokenCoordinador)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
        mockMvc.perform(get("/api/solicitud/pago-total/buscar/{id}",
                                solicitudPagoTotalConfirmado.getIdSolicitud())
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("PAGO_TOTAL_CONFIRMADO"))
                .andExpect(jsonPath("$.data.asignadaAlCoordinadorActual").value(true));
        assertEquals(EstadoSolicitud.PAGO_TOTAL_EFECTUADO,
                solicitudRepository.findById(solicitudPagoTotalEfectuado.getIdSolicitud())
                        .orElseThrow().getEstado());
    }

    @Test
    void detalleDeSolicitudIncluyeRecomendacionesPagosRecojoYFinalizacion() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        UsuarioEntity jefeUnidad = guardarUsuario(Rol.JEFE_UNIDAD);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        UsuarioEntity operario = guardarUsuario(Rol.OPERARIO);
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.FINALIZADO);
        recomendacionRepository.save(RecomendacionEntity.builder()
                .solicitud(solicitud)
                .cantidadUnidades(1)
                .razonCantidadUnidades("Una unidad")
                .equipoSeguridad("Guantes")
                .otrasRecomendaciones("Clasificar el cartón")
                .build());
        asignacionCoordinadorRepository.save(AsignacionCoordinadorEntity.builder()
                .solicitud(solicitud)
                .coordinador(coordinador)
                .build());
        LocalDateTime ahora = LocalDateTime.now();
        ProgramacionEntity programacion = programacionRepository.save(ProgramacionEntity.builder()
                .fechaRegistro(ahora.minusDays(2))
                .fechaEjecucion(ahora.minusDays(1))
                .fechaFinalizacion(ahora)
                .solicitud(solicitud)
                .coordinador(coordinador)
                .jefeUnidad(jefeUnidad)
                .estado(EstadoProgramacion.FINALIZADO)
                .build());
        VehiculoEntity vehiculo = vehiculoRepository.save(VehiculoEntity.builder()
                .fechaRegistro(ahora.minusDays(3))
                .placa(unique("PLACA"))
                .marca("Eco")
                .modelo("V1")
                .capacidadLibras(new BigDecimal("1000.00"))
                .tipo(TipoVehiculo.VOLQUETE)
                .estado(true)
                .situacion(SituacionVehiculo.DISPONIBLE)
                .build());
        DetalleEntity detalle = detalleRepository.save(DetalleEntity.builder()
                .transportista(transportista)
                .vehiculo(vehiculo)
                .programacion(programacion)
                .estado(true)
                .build());
        ColaEntity cola = colaRepository.save(ColaEntity.builder()
                .fechaRegistro(ahora.minusHours(2))
                .estado(false)
                .detalle(detalle)
                .build());
        MedioPagoEntity medioPago = medioPagoRepository.save(MedioPagoEntity.builder()
                .nombre("Transferencia")
                .descripcion("Transferencia bancaria")
                .numeroCuenta("123456789")
                .estado(true)
                .build());
        CargaEntity carga = cargaRepository.save(CargaEntity.builder()
                .fechaRegistro(ahora.minusHours(1))
                .pesoLibras(new BigDecimal("125.50"))
                .programacion(programacion)
                .operario(operario)
                .transportista(transportista)
                .cola(cola)
                .estado(EstadoCarga.REGISTRADA)
                .build());
        articuloRecuperadoRepository.save(ArticuloRecuperadoEntity.builder()
                .carga(carga)
                .articulo("Cartón")
                .cantidad(5)
                .build());
        pagoInicialRepository.save(PagoInicialEntity.builder()
                .monto(new BigDecimal("25.00"))
                .fechaOperacion(ahora.toLocalDate().minusDays(3))
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion(unique("PI"))
                .estado(EstadoPagoInicial.CONFIRMADO)
                .medioPago(medioPago)
                .solicitud(solicitud)
                .build());
        pagoTotalRepository.save(PagoTotalEntity.builder()
                .monto(new BigDecimal("75.00"))
                .fechaOperacion(ahora.toLocalDate().minusDays(2))
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion(unique("PT"))
                .estado(EstadoPagoTotal.CONFIRMADO)
                .medioPago(medioPago)
                .solicitud(solicitud)
                .build());

        mockMvc.perform(get("/api/solicitud/buscar/{id}", solicitud.getIdSolicitud())
                        .header("Authorization", "Bearer " + iniciarSesion(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.data.idCoordinador").value(coordinador.getIdUsuario()))
                .andExpect(jsonPath("$.data.cantidadUnidades").value(1))
                .andExpect(jsonPath("$.data.pagoInicial.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.data.pagoTotal.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.data.programacion.unidades.length()").value(1))
                .andExpect(jsonPath("$.data.ejecucion.pesoTotal").value(125.5))
                .andExpect(jsonPath("$.data.ejecucion.materiales.length()").value(1))
                .andExpect(jsonPath("$.data.finalizacion.estado").value("FINALIZADO"));
    }

    @Test
    void detalleDeSolicitudMuestraFasesIntermediasDePagoYProgramacion() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        UsuarioEntity jefeUnidad = guardarUsuario(Rol.JEFE_UNIDAD);
        String token = iniciarSesion(cliente);
        MedioPagoEntity medioPago = medioPagoRepository.save(MedioPagoEntity.builder()
                .nombre("Transferencia")
                .estado(true)
                .build());

        SolicitudEntity pagoInicialEfectuado = guardarSolicitud(
                cliente, EstadoSolicitud.PAGO_INICIAL_EFECTUADO);
        pagoInicialRepository.save(PagoInicialEntity.builder()
                .monto(new BigDecimal("25.00"))
                .fechaOperacion(LocalDate.now())
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion(unique("PI"))
                .estado(EstadoPagoInicial.PENDIENTE)
                .medioPago(medioPago)
                .solicitud(pagoInicialEfectuado)
                .build());
        SolicitudEntity pagoInicialPendiente = guardarSolicitud(
                cliente, EstadoSolicitud.PAGO_INICIAL_PENDIENTE);
        SolicitudEntity pagoTotalEfectuado = guardarSolicitud(
                cliente, EstadoSolicitud.PAGO_TOTAL_EFECTUADO);
        pagoTotalRepository.save(PagoTotalEntity.builder()
                .monto(new BigDecimal("75.00"))
                .fechaOperacion(LocalDate.now())
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion(unique("PT"))
                .estado(EstadoPagoTotal.PENDIENTE)
                .medioPago(medioPago)
                .solicitud(pagoTotalEfectuado)
                .build());
        SolicitudEntity pagoTotalPendiente = guardarSolicitud(
                cliente, EstadoSolicitud.PAGO_TOTAL_PENDIENTE);
        SolicitudEntity programada = guardarSolicitud(cliente, EstadoSolicitud.PROGRAMADO);
        programacionRepository.save(ProgramacionEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .fechaEjecucion(LocalDate.now().plusDays(1).atStartOfDay())
                .solicitud(programada)
                .coordinador(coordinador)
                .jefeUnidad(jefeUnidad)
                .estado(EstadoProgramacion.ASIGNADO)
                .build());

        mockMvc.perform(get("/api/solicitud/buscar/{id}", pagoInicialEfectuado.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fases[1].estado").value("EN_REVISION"));
        mockMvc.perform(get("/api/solicitud/buscar/{id}", pagoInicialPendiente.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fases[1].estado").value("PENDIENTE"));
        mockMvc.perform(get("/api/solicitud/buscar/{id}", pagoTotalEfectuado.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fases[1].estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.data.fases[2].estado").value("EN_REVISION"));
        mockMvc.perform(get("/api/solicitud/buscar/{id}", pagoTotalPendiente.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fases[2].estado").value("PENDIENTE"));
        mockMvc.perform(get("/api/solicitud/buscar/{id}", programada.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fases[3].estado").value("EN_PROCESO"))
                .andExpect(jsonPath("$.data.fases[4].estado").value("PENDIENTE"));
    }

    @Test
    void contratoSeGeneraSoloParaSolicitudFinalizadaConPagos() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        String token = iniciarSesion(cliente);
        SolicitudEntity enProceso = guardarSolicitud(cliente, EstadoSolicitud.PENDIENTE);
        SolicitudEntity incompleta = guardarSolicitud(cliente, EstadoSolicitud.FINALIZADO);
        MedioPagoEntity medioPago = medioPagoRepository.save(MedioPagoEntity.builder()
                .nombre("Transferencia")
                .descripcion("Pago de prueba")
                .numeroCuenta("123456789")
                .estado(true)
                .build());
        LocalDateTime finalizacion = LocalDateTime.now();
        SolicitudEntity finalizada = solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(finalizacion.minusDays(2))
                .fechaFinalizacion(finalizacion)
                .titulo("Contrato de prueba")
                .descripcion("Servicio de recojo")
                .ubicacion("Lima")
                .usuario(cliente)
                .estado(EstadoSolicitud.FINALIZADO)
                .build());
        pagoInicialRepository.save(PagoInicialEntity.builder()
                .monto(new BigDecimal("25.00"))
                .fechaOperacion(finalizacion.toLocalDate())
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion(unique("PI"))
                .estado(EstadoPagoInicial.CONFIRMADO)
                .medioPago(medioPago)
                .solicitud(finalizada)
                .build());
        pagoTotalRepository.save(PagoTotalEntity.builder()
                .monto(new BigDecimal("75.00"))
                .fechaOperacion(finalizacion.toLocalDate())
                .horaOperacion(LocalTime.NOON)
                .numeroOperacion(unique("PT"))
                .estado(EstadoPagoTotal.CONFIRMADO)
                .medioPago(medioPago)
                .solicitud(finalizada)
                .build());

        mockMvc.perform(get("/api/contrato/generar/{id}", 2_000_000)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/contrato/generar/{id}", enProceso.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/contrato/generar/{id}", incompleta.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        MvcResult pdf = mockMvc.perform(get("/api/contrato/generar/{id}", finalizada.getIdSolicitud())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        assertTrue(pdf.getResponse().getContentAsByteArray().length > 4);
        assertEquals("%PDF", new String(pdf.getResponse().getContentAsByteArray(),
                0, 4, StandardCharsets.US_ASCII));

        byte[] sinFirma = contratoService.generarContrato(
                "Administrador",
                "Cliente de prueba",
                String.valueOf(finalizada.getIdSolicitud()),
                "Servicio de recojo",
                new BigDecimal("25.00"),
                new BigDecimal("75.00"),
                "17/06/2026 12:00",
                null);
        assertEquals("%PDF", new String(sinFirma, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void endpointsDeSolicitudRechazanUsuarioAjenoRolIncorrectoYPaginasInvalidas() throws Exception {
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity otroCliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity coordinador = guardarUsuario(Rol.COORDINADOR);
        SolicitudEntity solicitudAjena = guardarSolicitud(otroCliente, EstadoSolicitud.PENDIENTE);
        SolicitudEntity solicitudPagoEfectuado = guardarSolicitud(
                otroCliente, EstadoSolicitud.PAGO_TOTAL_EFECTUADO);
        String tokenCliente = iniciarSesion(cliente);
        String tokenCoordinador = iniciarSesion(coordinador);

        mockMvc.perform(get("/api/solicitud/total")
                        .with(user("usuario-inexistente").roles("USUARIO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/paginas")
                        .with(user("usuario-inexistente").roles("USUARIO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/solicitud/registrar")
                        .with(user("usuario-inexistente").roles("USUARIO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(SolicitudRequest.builder()
                                .titulo("Sin usuario")
                                .descripcion("No debe persistir")
                                .ubicacion("Lima")
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/pendientes/total")
                        .with(user(cliente.getUsername()).roles("COORDINADOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/pendientes/total")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/solicitud/listar").header("Authorization", "Bearer " + tokenCliente)
                        .param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/pendientes/listar")
                        .header("Authorization", "Bearer " + tokenCoordinador)
                        .param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/buscar/{id}", solicitudAjena.getIdSolicitud())
                        .header("Authorization", "Bearer " + iniciarSesion(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/pendientes/buscar/{id}",
                                solicitudPagoEfectuado.getIdSolicitud())
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/pi-confirmadas/buscar/{id}",
                                solicitudAjena.getIdSolicitud())
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/solicitud/pago-total/buscar/{id}",
                                solicitudPagoEfectuado.getIdSolicitud())
                        .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private UsuarioEntity guardarUsuario(Rol rol) {
        return usuarioRepository.save(UsuarioEntity.builder()
                .nombre("Usuario")
                .apellido("Integration")
                .dni(unique("dni"))
                .correo(unique("mail") + "@example.com")
                .celular("900000001")
                .username(unique(rol.name().toLowerCase()))
                .password(passwordEncoder.encode(PASSWORD))
                .estado(true)
                .rol(rol)
                .situacion(rol == Rol.USUARIO ? SituacionUsuario.NINGUNO : SituacionUsuario.DISPONIBLE)
                .build());
    }

    private SolicitudEntity guardarSolicitud(UsuarioEntity usuario, EstadoSolicitud estado) {
        return solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo("Solicitud " + estado)
                .descripcion("Solicitud de integración para " + estado)
                .ubicacion("Lima")
                .usuario(usuario)
                .estado(estado)
                .build());
    }

    private void guardarRecomendacion(SolicitudEntity solicitud) {
        recomendacionRepository.save(RecomendacionEntity.builder()
                .solicitud(solicitud)
                .cantidadUnidades(1)
                .razonCantidadUnidades("Una unidad es suficiente")
                .equipoSeguridad("Guantes")
                .otrasRecomendaciones("Ninguna")
                .build());
    }

    private String iniciarSesion(UsuarioEntity usuario) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/usuario/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(usuario.getUsername(), PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode respuesta = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return respuesta.path("data").asText();
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
