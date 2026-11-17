package org.ecotrack_project.integration;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Transactional
class OperationsIntegrationIT {

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
    private ProgramacionRepository programacionRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private DetalleRepository detalleRepository;
    @Autowired
    private ColaRepository colaRepository;
    @Autowired
    private CargaRepository cargaRepository;
    @Autowired
    private ReservaUnidadRepository reservaUnidadRepository;
    @Autowired
    private ArticuloRecuperadoRepository articuloRecuperadoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void reservarUnidadDeEmergenciaPersisteLaReservaYActualizaRecursos() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.DISPONIBLE));
        String token = iniciarSesion(administrador);

        mockMvc.perform(post("/api/reserva-unidad/{id}/reservar?tipo=VOLQUETE",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/reserva-unidad/{id}", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(post("/api/reserva-unidad/{id}/confirmar", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        assertTrue(reservaUnidadRepository.findByProgramacion_IdProgramacionOrderByIdReservaAsc(
                programacion.getIdProgramacion()).isEmpty());
        assertEquals(SituacionVehiculo.ASIGNADO,
                vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.ASIGNADO,
                usuarioRepository.findById(transportista.getIdUsuario()).orElseThrow().getSituacion());
    }

    @Test
    void liberarReservaEmergenciaEliminaLaReservaYDevuelveRecursos() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.DISPONIBLE));
        String token = iniciarSesion(administrador);
        MvcResult resultado = mockMvc.perform(post("/api/reserva-unidad/{id}/reservar?tipo=VOLQUETE",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        int idReserva = objectMapper.readTree(resultado.getResponse().getContentAsString())
                .path("data").path("idReserva").asInt();

        mockMvc.perform(delete("/api/reserva-unidad/{idProgramacion}/{idReserva}",
                                programacion.getIdProgramacion(), idReserva)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertTrue(reservaUnidadRepository.findById(idReserva).isEmpty());
        assertEquals(SituacionVehiculo.DISPONIBLE,
                vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.DISPONIBLE,
                usuarioRepository.findById(transportista.getIdUsuario()).orElseThrow().getSituacion());
    }

    @Test
    void cancelarReservasEmergenciaLiberaTodasLasReservasDelUsuario() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.DISPONIBLE));
        String token = iniciarSesion(administrador);
        mockMvc.perform(post("/api/reserva-unidad/{id}/reservar?tipo=VOLQUETE",
                        programacion.getIdProgramacion())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/reserva-unidad/{id}/cancelar", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));

        assertTrue(reservaUnidadRepository.findByProgramacion_IdProgramacionOrderByIdReservaAsc(
                programacion.getIdProgramacion()).isEmpty());
        assertEquals(SituacionVehiculo.DISPONIBLE,
                vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.DISPONIBLE,
                usuarioRepository.findById(transportista.getIdUsuario()).orElseThrow().getSituacion());
    }

    @Test
    void reservarUnidadRechazaRecursosAgotadosYLiberaReservasVencidas() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        String token = iniciarSesion(administrador);
        String ruta = "/api/reserva-unidad/{id}/reservar";

        mockMvc.perform(post(ruta, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        VehiculoEntity disponible = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.DISPONIBLE));
        mockMvc.perform(post(ruta, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        UsuarioEntity transportistaDisponible = guardarUsuario(Rol.TRANSPORTISTA);
        VehiculoEntity vehiculoVencido = vehiculoRepository.save(
                guardarVehiculo(SituacionVehiculo.RESERVADO));
        UsuarioEntity transportistaVencido = guardarUsuario(Rol.TRANSPORTISTA);
        transportistaVencido.setSituacion(SituacionUsuario.RESERVADO);
        usuarioRepository.save(transportistaVencido);
        LocalDateTime vencimiento = LocalDateTime.now().minusMinutes(1);
        ReservaUnidadEntity reservaVencida = reservaUnidadRepository.saveAndFlush(
                ReservaUnidadEntity.builder()
                        .fechaReserva(vencimiento.minusMinutes(15))
                        .fechaExpiracion(vencimiento)
                        .programacion(programacion)
                        .transportista(transportistaVencido)
                        .vehiculo(vehiculoVencido)
                        .usuarioReserva(administrador)
                        .build());

        mockMvc.perform(post(ruta, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertTrue(reservaUnidadRepository.findById(reservaVencida.getIdReserva()).isEmpty());
        assertEquals(SituacionVehiculo.DISPONIBLE,
                vehiculoRepository.findById(vehiculoVencido.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.DISPONIBLE,
                usuarioRepository.findById(transportistaVencido.getIdUsuario()).orElseThrow().getSituacion());
        assertEquals(SituacionVehiculo.RESERVADO,
                vehiculoRepository.findById(disponible.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.RESERVADO,
                usuarioRepository.findById(transportistaDisponible.getIdUsuario()).orElseThrow().getSituacion());
    }

    @Test
    void reservasSeFiltranPorPropietarioYRechazanLiberacionOConfirmacionInvalida() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity otroAdministrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        UsuarioEntity otroTransportista = guardarUsuario(Rol.TRANSPORTISTA);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.DISPONIBLE));
        VehiculoEntity otroVehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.RESERVADO));
        otroTransportista.setSituacion(SituacionUsuario.RESERVADO);
        usuarioRepository.save(otroTransportista);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        ProgramacionEntity otraProgramacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        String token = iniciarSesion(administrador);
        String otroToken = iniciarSesion(otroAdministrador);
        LocalDateTime ahora = LocalDateTime.now();

        MvcResult resultado = mockMvc.perform(post("/api/reserva-unidad/{id}/reservar?tipo=VOLQUETE",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        int idReservaPropia = objectMapper.readTree(resultado.getResponse().getContentAsString())
                .path("data").path("idReserva").asInt();
        ReservaUnidadEntity reservaAjena = reservaUnidadRepository.save(
                ReservaUnidadEntity.builder()
                        .fechaReserva(ahora)
                        .fechaExpiracion(ahora.plusMinutes(15))
                        .programacion(programacion)
                        .transportista(otroTransportista)
                        .vehiculo(otroVehiculo)
                        .usuarioReserva(otroAdministrador)
                        .build());

        mockMvc.perform(get("/api/reserva-unidad/{id}", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/reserva-unidad/{id}", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + otroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(delete("/api/reserva-unidad/{idProgramacion}/{idReserva}",
                                programacion.getIdProgramacion(), reservaAjena.getIdReserva())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/reserva-unidad/{idProgramacion}/{idReserva}",
                                otraProgramacion.getIdProgramacion(), idReservaPropia)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reserva-unidad/{id}/confirmar",
                                otraProgramacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/reserva-unidad/{id}/cancelar",
                                otraProgramacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));

        vehiculo.setSituacion(SituacionVehiculo.DISPONIBLE);
        vehiculoRepository.save(vehiculo);
        mockMvc.perform(post("/api/reserva-unidad/{id}/confirmar",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        transportista.setSituacion(SituacionUsuario.ASIGNADO);
        usuarioRepository.save(transportista);
        mockMvc.perform(delete("/api/reserva-unidad/{id}/cancelar",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void reservaUnidadValidaAutenticacionPermisosYEstadoDeProgramacion() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity jefeUnidad = guardarUsuario(Rol.JEFE_UNIDAD);
        ProgramacionEntity programacionAdmin = guardarProgramacion(
                administrador, EstadoProgramacion.EJECUTANDO);
        ProgramacionEntity programacionJefe = guardarProgramacion(
                jefeUnidad, EstadoProgramacion.EJECUTANDO);
        ProgramacionEntity programacionAsignada = guardarProgramacion(
                jefeUnidad, EstadoProgramacion.ASIGNADO);
        String tokenAdmin = iniciarSesion(administrador);
        String tokenJefe = iniciarSesion(jefeUnidad);
        String ruta = "/api/reserva-unidad/{id}/reservar";

        mockMvc.perform(post(ruta, programacionAdmin.getIdProgramacion())
                        .with(user("sesion-sin-usuario-details").roles("ADMINISTRADOR"))
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(ruta, 2_000_000)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(ruta, programacionAdmin.getIdProgramacion())
                        .header("Authorization", "Bearer " + tokenJefe)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(ruta, programacionAsignada.getIdProgramacion())
                        .header("Authorization", "Bearer " + tokenJefe)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(ruta, programacionJefe.getIdProgramacion())
                        .header("Authorization", "Bearer " + tokenJefe)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest());

        vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.DISPONIBLE));
        guardarUsuario(Rol.TRANSPORTISTA);
        MvcResult resultado = mockMvc.perform(post(ruta, programacionJefe.getIdProgramacion())
                        .header("Authorization", "Bearer " + tokenJefe)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isOk())
                .andReturn();
        int idReserva = objectMapper.readTree(resultado.getResponse().getContentAsString())
                .path("data").path("idReserva").asInt();

        mockMvc.perform(delete("/api/reserva-unidad/{idProgramacion}/{idReserva}",
                                programacionJefe.getIdProgramacion(), idReserva)
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk());
    }

    @Test
    void operarioIniciaYFinalizaCargaRegistrandoArticulosRecuperados() throws Exception {
        UsuarioEntity operario = guardarUsuario(Rol.OPERARIO);
        UsuarioEntity otroOperario = guardarUsuario(Rol.OPERARIO);
        UsuarioEntity jefeUnidad = guardarUsuario(Rol.JEFE_UNIDAD);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.ASIGNADO));
        DetalleEntity detalle = detalleRepository.save(DetalleEntity.builder()
                .programacion(programacion)
                .transportista(transportista)
                .vehiculo(vehiculo)
                .estado(true)
                .build());
        ColaEntity cola = colaRepository.save(ColaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .estado(true)
                .detalle(detalle)
                .build());
        ColaEntity colaSinCarga = colaRepository.save(ColaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .estado(false)
                .detalle(detalle)
                .build());
        String token = iniciarSesion(operario);
        String tokenOtroOperario = iniciarSesion(otroOperario);
        String tokenJefeUnidad = iniciarSesion(jefeUnidad);

        mockMvc.perform(get("/api/carga/salida/{idCola}", colaSinCarga.getIdCola())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));

        MvcResult inicio = mockMvc.perform(post("/api/carga/iniciar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCola":%d,"pesoLibras":125.50}
                                """.formatted(cola.getIdCola())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        JsonNode inicioJson = objectMapper.readTree(inicio.getResponse().getContentAsString());
        int idCarga = inicioJson.path("data").path("idCarga").asInt();

        mockMvc.perform(get("/api/carga/salida/{idCola}", cola.getIdCola())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.idCarga").value(idCarga));

        mockMvc.perform(post("/api/carga/iniciar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCola":%d,"pesoLibras":150.25}
                                """.formatted(cola.getIdCola())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.idCarga").value(idCarga));
        assertEquals(new BigDecimal("150.25"),
                cargaRepository.findById(idCarga).orElseThrow().getPesoLibras());

        mockMvc.perform(post("/api/carga/iniciar")
                        .header("Authorization", "Bearer " + tokenOtroOperario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCola":%d,"pesoLibras":125.50}
                                """.formatted(cola.getIdCola())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/carga/salida/{idCola}", cola.getIdCola())
                        .header("Authorization", "Bearer " + tokenOtroOperario))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/carga/programacion/{id}/listar", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + tokenJefeUnidad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/carga/finalizar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCarga":%d,"articulos":[null]}
                                """.formatted(idCarga)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/carga/finalizar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCarga":%d,"articulos":[{"articulo":"Botellas","cantidad":0}]}
                                """.formatted(idCarga)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/carga/finalizar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCarga":%d,"articulos":[{"articulo":"Botellas","cantidad":3}]}
                                """.formatted(idCarga)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        CargaEntity carga = cargaRepository.findById(idCarga).orElseThrow();
        assertEquals(EstadoCarga.REGISTRADA, carga.getEstado());
        assertEquals(1, articuloRecuperadoRepository.findByCarga_IdCarga(idCarga).size());
        assertEquals(new BigDecimal("150.25"), carga.getPesoLibras());

        mockMvc.perform(get("/api/carga/programacion/{id}/listar", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        String tokenAdmin = iniciarSesion(administrador);
        mockMvc.perform(get("/api/carga/rendimiento/paginas")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("fechaInicio", "2026-11-15T00:00:00")
                        .param("fechaFin", "2026-11-17T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/carga/iniciar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCola":%d,"pesoLibras":125.50}
                                """.formatted(cola.getIdCola())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/carga/finalizar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCarga":%d,"articulos":[]}
                                """.formatted(idCarga)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(get("/api/carga/rendimiento/listar/0")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("fechaInicio", "2026-11-15T00:00:00")
                        .param("fechaFin", "2026-11-17T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void registrarCargaConArticulosYListarCargasPorProgramacion() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.ASIGNADO));
        DetalleEntity detalle = detalleRepository.save(DetalleEntity.builder()
                .programacion(programacion)
                .transportista(transportista)
                .vehiculo(vehiculo)
                .estado(true)
                .build());
        ColaEntity cola = colaRepository.save(ColaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .estado(false)
                .detalle(detalle)
                .build());
        String token = iniciarSesion(administrador);

        mockMvc.perform(post("/api/carga/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCola":%d,"pesoLibras":80.00,"articulos":[{"articulo":"Cartón","cantidad":5}]}
                                """.formatted(cola.getIdCola())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(1, cargaRepository.findByProgramacion_IdProgramacionOrderByFechaRegistroDesc(
                programacion.getIdProgramacion()).size());
        assertEquals(1, articuloRecuperadoRepository.findByCarga_IdCarga(
                cargaRepository.findByCola_IdCola(cola.getIdCola()).orElseThrow().getIdCarga()).size());
    }

    @Test
    void registrarCargaCompletaProgramacionQueEsperabaFinalizacion() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.POR_FINALIZAR);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.ASIGNADO));
        DetalleEntity detalle = detalleRepository.save(DetalleEntity.builder()
                .programacion(programacion)
                .transportista(transportista)
                .vehiculo(vehiculo)
                .estado(true)
                .build());
        ColaEntity cola = colaRepository.save(ColaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .estado(false)
                .detalle(detalle)
                .build());
        String token = iniciarSesion(administrador);

        mockMvc.perform(post("/api/carga/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCola":%d,"pesoLibras":80.00,"articulos":[{"articulo":"Cartón","cantidad":5}]}
                                """.formatted(cola.getIdCola())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoProgramacion.FINALIZADO,
                programacionRepository.findById(programacion.getIdProgramacion()).orElseThrow().getEstado());
        assertEquals(SituacionVehiculo.DISPONIBLE,
                vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.DISPONIBLE,
                usuarioRepository.findById(transportista.getIdUsuario()).orElseThrow().getSituacion());
    }

    @Test
    void registrarCargaRechazaEntradasInvalidasYSalidasNoRecepcionadas() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        ProgramacionEntity programacion = guardarProgramacion(administrador, EstadoProgramacion.EJECUTANDO);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.ASIGNADO));
        DetalleEntity detalle = detalleRepository.save(DetalleEntity.builder()
                .programacion(programacion)
                .transportista(transportista)
                .vehiculo(vehiculo)
                .estado(true)
                .build());
        ColaEntity cola = colaRepository.save(ColaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .estado(true)
                .detalle(detalle)
                .build());
        String token = iniciarSesion(administrador);
        String request = """
                {"idCola":%d,"pesoLibras":80.00,"articulos":%s}
                """;

        mockMvc.perform(post("/api/carga/registrar")
                        .with(user("sesion-sin-usuario-details").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted(cola.getIdCola(),
                                "[{\"articulo\":\"Cartón\",\"cantidad\":1}]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/carga/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted(cola.getIdCola(), "[]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/carga/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted(cola.getIdCola(),
                                "[{\"articulo\":\"  \",\"cantidad\":1}]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/carga/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted(99999,
                                "[{\"articulo\":\"Cartón\",\"cantidad\":1}]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/carga/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted(cola.getIdCola(),
                                "[{\"articulo\":\"Cartón\",\"cantidad\":1}]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void recepcionarUltimaSalidaFinalizaProgramacionYLiberaRecursos() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        SolicitudEntity solicitud = solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo("Solicitud de cierre")
                .descripcion("Solicitud para validar finalización")
                .ubicacion("Lima")
                .estado(EstadoSolicitud.PROGRAMADO)
                .usuario(guardarUsuario(Rol.USUARIO))
                .build());
        ProgramacionEntity programacion = programacionRepository.save(ProgramacionEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .fechaEjecucion(LocalDate.now().atStartOfDay())
                .solicitud(solicitud)
                .coordinador(administrador)
                .jefeUnidad(administrador)
                .estado(EstadoProgramacion.POR_FINALIZAR)
                .build());
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo(SituacionVehiculo.ASIGNADO));
        DetalleEntity detalle = detalleRepository.save(DetalleEntity.builder()
                .programacion(programacion)
                .transportista(transportista)
                .vehiculo(vehiculo)
                .estado(true)
                .build());
        ColaEntity cola = colaRepository.save(ColaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .estado(true)
                .detalle(detalle)
                .build());
        cargaRepository.save(CargaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .pesoLibras(new BigDecimal("75.00"))
                .programacion(programacion)
                .operario(administrador)
                .transportista(transportista)
                .cola(cola)
                .estado(EstadoCarga.REGISTRADA)
                .build());
        String token = iniciarSesion(administrador);

        mockMvc.perform(get("/api/cola/programacion/{id}/listar", programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(put("/api/cola/{id}/recepcionar", cola.getIdCola())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(EstadoProgramacion.FINALIZADO,
                programacionRepository.findById(programacion.getIdProgramacion()).orElseThrow().getEstado());
        assertEquals(EstadoSolicitud.FINALIZADO,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());
        assertEquals(SituacionVehiculo.DISPONIBLE,
                vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.DISPONIBLE,
                usuarioRepository.findById(transportista.getIdUsuario()).orElseThrow().getSituacion());
    }

    private UsuarioEntity guardarUsuario(Rol rol) {
        SituacionUsuario situacion = switch (rol) {
            case USUARIO -> SituacionUsuario.NINGUNO;
            case TRANSPORTISTA, JEFE_UNIDAD, COORDINADOR, OPERARIO, ADMINISTRADOR -> SituacionUsuario.DISPONIBLE;
        };
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
                .situacion(situacion)
                .build());
    }

    private SolicitudEntity guardarSolicitud(UsuarioEntity usuario) {
        return solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo("Programación de prueba")
                .descripcion("Solicitud de integración")
                .ubicacion("Lima")
                .estado(EstadoSolicitud.PROGRAMADO)
                .usuario(usuario)
                .build());
    }

    private ProgramacionEntity guardarProgramacion(UsuarioEntity coordinador, EstadoProgramacion estado) {
        SolicitudEntity solicitud = guardarSolicitud(guardarUsuario(Rol.USUARIO));
        return programacionRepository.save(ProgramacionEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .fechaEjecucion(LocalDate.now().atStartOfDay())
                .solicitud(solicitud)
                .coordinador(coordinador)
                .jefeUnidad(coordinador)
                .estado(estado)
                .build());
    }

    private VehiculoEntity guardarVehiculo(SituacionVehiculo situacion) {
        return VehiculoEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .placa(unique("PLACA"))
                .marca("Eco")
                .modelo("V1")
                .capacidadLibras(new BigDecimal("1000.00"))
                .tipo(TipoVehiculo.VOLQUETE)
                .estado(true)
                .situacion(situacion)
                .build();
    }

    private String iniciarSesion(UsuarioEntity usuario) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/usuario/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(usuario.getUsername(), PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .path("data")
                .asText();
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
