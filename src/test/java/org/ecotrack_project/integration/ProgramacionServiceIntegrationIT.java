package org.ecotrack_project.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecotrack_project.dto.request.ProgramacionRequest;
import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.entity.RecomendacionEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.repository.DetalleRepository;
import org.ecotrack_project.repository.ProgramacionRepository;
import org.ecotrack_project.repository.RecomendacionRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class ProgramacionServiceIntegrationIT {

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
    private ProgramacionRepository programacionRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private DetalleRepository detalleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void endpointsDeProgramacionCubrenAsignacionUnidadesEjecucionYFinalizacion() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity jefeUnidad = guardarUsuario(Rol.JEFE_UNIDAD);
        guardarUsuario(Rol.JEFE_UNIDAD);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo());
        SolicitudEntity solicitud = solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo("Retiro de materiales")
                .descripcion("Solicitud para recojo")
                .ubicacion("Lima")
                .usuario(cliente)
                .estado(EstadoSolicitud.PAGO_TOTAL_CONFIRMADO)
                .build());
        recomendacionRepository.save(RecomendacionEntity.builder()
                .solicitud(solicitud)
                .cantidadUnidades(1)
                .razonCantidadUnidades("Una unidad")
                .equipoSeguridad("Guantes")
                .otrasRecomendaciones("Ninguna")
                .build());
        String token = iniciarSesion(administrador);
        LocalDate fechaEjecucion = LocalDate.of(2026, 11, 17);

        MvcResult registro = mockMvc.perform(post("/api/programacion/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(ProgramacionRequest.builder()
                                .idSolicitud(solicitud.getIdSolicitud())
                                .fechaEjecucion(fechaEjecucion)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        int idProgramacion = objectMapper.readTree(registro.getResponse().getContentAsString())
                .path("data").asInt();

        assertEquals(EstadoProgramacion.ASIGNADO,
                programacionRepository.findById(idProgramacion).orElseThrow().getEstado());
        assertEquals(EstadoSolicitud.PROGRAMADO,
                solicitudRepository.findById(solicitud.getIdSolicitud()).orElseThrow().getEstado());

        mockMvc.perform(get("/api/programacion/fecha/total")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", fechaEjecucion.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/programacion/fecha/paginas")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", fechaEjecucion.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/programacion/fecha/listar")
                        .header("Authorization", "Bearer " + token)
                        .param("fecha", fechaEjecucion.toString())
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/programacion/detalle/{id}", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.idProgramacion").value(idProgramacion));

        mockMvc.perform(get("/api/programacion/mis-programaciones/total")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/programacion/mis-programaciones/paginas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/programacion/mis-programaciones/listar")
                        .header("Authorization", "Bearer " + token)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/programacion/mis-programaciones/asignadas/total")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/programacion/mis-programaciones/{id}/detalle", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("ASIGNADO"));

        mockMvc.perform(get("/api/programacion/mis-programaciones/{id}/unidades", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        MvcResult primeraUnidad = mockMvc.perform(post(
                                "/api/programacion/mis-programaciones/{id}/unidades", idProgramacion)
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        int idPrimerDetalle = objectMapper.readTree(primeraUnidad.getResponse().getContentAsString())
                .path("data").path("idDetalle").asInt();
        mockMvc.perform(delete(
                                "/api/programacion/mis-programaciones/{id}/unidades/{idDetalle}",
                                idProgramacion, idPrimerDetalle)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(idPrimerDetalle));
        assertEquals(SituacionVehiculo.DISPONIBLE,
                vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.DISPONIBLE,
                usuarioRepository.findById(transportista.getIdUsuario()).orElseThrow().getSituacion());
        assertFalse(detalleRepository.findById(idPrimerDetalle).orElseThrow().isEstado());

        MvcResult unidadActiva = mockMvc.perform(post(
                                "/api/programacion/mis-programaciones/{id}/unidades", idProgramacion)
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isOk())
                .andReturn();
        int idDetalle = objectMapper.readTree(unidadActiva.getResponse().getContentAsString())
                .path("data").path("idDetalle").asInt();
        mockMvc.perform(get("/api/programacion/mis-programaciones/{id}/unidades", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(put("/api/programacion/mis-programaciones/{id}/ejecutar", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(idProgramacion));
        mockMvc.perform(get("/api/programacion/ejecutadas/total")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/programacion/ejecutadas/paginas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
        mockMvc.perform(get("/api/programacion/ejecutadas/listar")
                        .header("Authorization", "Bearer " + token)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get("/api/programacion/mis-programaciones/{id}/detalle", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("EJECUTANDO"));

        mockMvc.perform(post("/api/programacion/mis-programaciones/{id}/cola/{idDetalle}",
                                idProgramacion, idDetalle)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/programacion/{id}/cola/pendientes", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(put("/api/programacion/mis-programaciones/{id}/finalizar", idProgramacion)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(idProgramacion));
        assertEquals(EstadoProgramacion.POR_FINALIZAR,
                programacionRepository.findById(idProgramacion).orElseThrow().getEstado());
    }

    @Test
    void registrarProgramacionRechazaFechasEstadosDuplicadosYSinJefesActivos() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        String token = iniciarSesion(administrador);
        SolicitudEntity confirmadaFinDeSemana = guardarSolicitud(cliente, EstadoSolicitud.PAGO_TOTAL_CONFIRMADO);
        SolicitudEntity pendiente = guardarSolicitud(cliente, EstadoSolicitud.PENDIENTE);
        SolicitudEntity confirmadaDuplicada = guardarSolicitud(cliente, EstadoSolicitud.PAGO_TOTAL_CONFIRMADO);
        SolicitudEntity confirmadaSinJefe = guardarSolicitud(cliente, EstadoSolicitud.PAGO_TOTAL_CONFIRMADO);
        programacionRepository.save(ProgramacionEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .fechaEjecucion(LocalDate.of(2026, 11, 23).atStartOfDay())
                .solicitud(confirmadaDuplicada)
                .coordinador(administrador)
                .jefeUnidad(administrador)
                .estado(EstadoProgramacion.ASIGNADO)
                .build());

        registrarProgramacion(token, confirmadaFinDeSemana.getIdSolicitud(), LocalDate.of(2026, 11, 22))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        registrarProgramacion(token, pendiente.getIdSolicitud(), LocalDate.of(2026, 11, 23))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        registrarProgramacion(token, 99999, LocalDate.of(2026, 11, 23))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        registrarProgramacion(token, confirmadaDuplicada.getIdSolicitud(), LocalDate.of(2026, 11, 23))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        registrarProgramacion(token, confirmadaSinJefe.getIdSolicitud(), LocalDate.of(2026, 11, 23))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void consultasDeProgramacionFuncionanParaJefeTransportistaYOperario() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        UsuarioEntity jefeUnidad = guardarUsuario(Rol.JEFE_UNIDAD);
        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        UsuarioEntity operario = guardarUsuario(Rol.OPERARIO);
        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo());
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.PROGRAMADO);
        ProgramacionEntity programacion = programacionRepository.save(ProgramacionEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .fechaEjecucion(null)
                .solicitud(solicitud)
                .coordinador(administrador)
                .jefeUnidad(jefeUnidad)
                .estado(EstadoProgramacion.ASIGNADO)
                .build());
        detalleRepository.save(org.ecotrack_project.entity.DetalleEntity.builder()
                .transportista(transportista)
                .vehiculo(vehiculo)
                .programacion(programacion)
                .estado(true)
                .build());

        String tokenJefe = iniciarSesion(jefeUnidad);
        String tokenTransportista = iniciarSesion(transportista);
        String tokenOperario = iniciarSesion(operario);

        for (String token : new String[]{tokenJefe, tokenTransportista}) {
            mockMvc.perform(get("/api/programacion/mis-programaciones/total")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(1));
            mockMvc.perform(get("/api/programacion/mis-programaciones/paginas")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(1));
            mockMvc.perform(get("/api/programacion/mis-programaciones/listar")
                            .header("Authorization", "Bearer " + token)
                            .param("pagina", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
            mockMvc.perform(get("/api/programacion/mis-programaciones/asignadas/total")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(1));
            mockMvc.perform(get("/api/programacion/mis-programaciones/{id}/unidades",
                                    programacion.getIdProgramacion())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1));
            mockMvc.perform(get("/api/programacion/mis-programaciones/{id}/detalle",
                                    programacion.getIdProgramacion())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.idProgramacion").value(programacion.getIdProgramacion()));
        }

        mockMvc.perform(get("/api/programacion/ejecutadas/total")
                        .header("Authorization", "Bearer " + tokenOperario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
        mockMvc.perform(get("/api/programacion/ejecutadas/paginas")
                        .header("Authorization", "Bearer " + tokenOperario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
        mockMvc.perform(get("/api/programacion/ejecutadas/listar")
                        .header("Authorization", "Bearer " + tokenOperario)
                        .param("pagina", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/api/programacion/{id}/cola/pendientes",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + tokenJefe))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void gestionDeUnidadesYSalidasRechazaEstadosYRecursosInvalidos() throws Exception {
        UsuarioEntity administrador = guardarUsuario(Rol.ADMINISTRADOR);
        UsuarioEntity cliente = guardarUsuario(Rol.USUARIO);
        SolicitudEntity solicitud = guardarSolicitud(cliente, EstadoSolicitud.PROGRAMADO);
        ProgramacionEntity programacion = programacionRepository.save(ProgramacionEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .solicitud(solicitud)
                .coordinador(administrador)
                .jefeUnidad(administrador)
                .estado(EstadoProgramacion.ASIGNADO)
                .build());
        String token = iniciarSesion(administrador);
        String unidadesUrl = "/api/programacion/mis-programaciones/{id}/unidades";

        mockMvc.perform(post(unidadesUrl, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        programacion.setFechaEjecucion(LocalDate.of(2026, 11, 17).atStartOfDay());
        programacionRepository.saveAndFlush(programacion);
        mockMvc.perform(post(unidadesUrl, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        RecomendacionEntity recomendacion = recomendacionRepository.save(RecomendacionEntity.builder()
                .solicitud(solicitud)
                .cantidadUnidades(0)
                .razonCantidadUnidades("Sin unidades")
                .build());
        mockMvc.perform(post(unidadesUrl, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        recomendacion.setCantidadUnidades(1);
        recomendacionRepository.saveAndFlush(recomendacion);
        mockMvc.perform(post(unidadesUrl, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        VehiculoEntity vehiculo = vehiculoRepository.save(guardarVehiculo());
        mockMvc.perform(post(unidadesUrl, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        UsuarioEntity transportista = guardarUsuario(Rol.TRANSPORTISTA);
        MvcResult unidad = mockMvc.perform(post(unidadesUrl, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isOk())
                .andReturn();
        int idDetalle = objectMapper.readTree(unidad.getResponse().getContentAsString())
                .path("data").path("idDetalle").asInt();
        assertEquals(SituacionVehiculo.ASIGNADO,
                vehiculoRepository.findById(vehiculo.getIdVehiculo()).orElseThrow().getSituacion());
        assertEquals(SituacionUsuario.ASIGNADO,
                usuarioRepository.findById(transportista.getIdUsuario()).orElseThrow().getSituacion());

        mockMvc.perform(post(unidadesUrl, programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", TipoVehiculo.VOLQUETE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(put("/api/programacion/mis-programaciones/{id}/ejecutar",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/programacion/mis-programaciones/{id}/ejecutar",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/programacion/mis-programaciones/{id}/cola/{idDetalle}",
                                programacion.getIdProgramacion(), idDetalle + 1)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(post("/api/programacion/mis-programaciones/{id}/cola/{idDetalle}",
                                programacion.getIdProgramacion(), idDetalle)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/programacion/mis-programaciones/{id}/cola/{idDetalle}",
                                programacion.getIdProgramacion(), idDetalle)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        mockMvc.perform(put("/api/programacion/mis-programaciones/{id}/finalizar",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/programacion/mis-programaciones/{id}/finalizar",
                                programacion.getIdProgramacion())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private org.springframework.test.web.servlet.ResultActions registrarProgramacion(
            String token,
            int idSolicitud,
            LocalDate fecha
    ) throws Exception {
        return mockMvc.perform(post("/api/programacion/registrar")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(ProgramacionRequest.builder()
                        .idSolicitud(idSolicitud)
                        .fechaEjecucion(fecha)
                        .build())));
    }

    private SolicitudEntity guardarSolicitud(UsuarioEntity usuario, EstadoSolicitud estado) {
        return solicitudRepository.save(SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo("Solicitud de prueba")
                .descripcion("Solicitud para integración")
                .ubicacion("Lima")
                .usuario(usuario)
                .estado(estado)
                .build());
    }

    private UsuarioEntity guardarUsuario(Rol rol) {
        SituacionUsuario situacion = switch (rol) {
            case USUARIO -> SituacionUsuario.NINGUNO;
            case TRANSPORTISTA, JEFE_UNIDAD, COORDINADOR, OPERARIO, ADMINISTRADOR ->
                    SituacionUsuario.DISPONIBLE;
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

    private VehiculoEntity guardarVehiculo() {
        return VehiculoEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .placa(unique("PLACA"))
                .marca("Eco")
                .modelo("V1")
                .capacidadLibras(new BigDecimal("1000.00"))
                .tipo(TipoVehiculo.VOLQUETE)
                .estado(true)
                .situacion(SituacionVehiculo.DISPONIBLE)
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
        JsonNode respuesta = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return respuesta.path("data").asText();
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
