package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.ArticuloRecuperadoRequest;
import org.ecotrack_project.dto.request.FinalizarCargaRequest;
import org.ecotrack_project.dto.request.IniciarCargaRequest;
import org.ecotrack_project.dto.response.CantidadCargasResponse;
import org.ecotrack_project.dto.response.CargaProcesoResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.EstadoCarga;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.ecotrack_project.dto.request.CargaRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.CargaResponse;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.security.UsuarioDetails;
import org.ecotrack_project.service.CargaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CargaServiceImpl implements CargaService {

    private final CargaRepository cargaRepository;
    private final ColaRepository colaRepository;
    private final ProgramacionRepository programacionRepository;
    private final LiberacionRecursosService liberacionRecursosService;
    private final ArticuloRecuperadoRepository articuloRecuperadoRepository;
    private final FinalizacionProgramacionService finalizacionProgramacionService;
    private final UsuarioRepository usuarioRepository;

    public CargaServiceImpl(
            CargaRepository cargaRepository,
            ColaRepository colaRepository,
            ProgramacionRepository programacionRepository,
            LiberacionRecursosService liberacionRecursosService,
            ArticuloRecuperadoRepository articuloRecuperadoRepository,
            UsuarioRepository usuarioRepository,
            FinalizacionProgramacionService finalizacionProgramacionService
    ) {
        this.cargaRepository = cargaRepository;
        this.colaRepository = colaRepository;
        this.programacionRepository = programacionRepository;
        this.liberacionRecursosService = liberacionRecursosService;
        this.articuloRecuperadoRepository = articuloRecuperadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.finalizacionProgramacionService = finalizacionProgramacionService;
    }

    @Override
    @Transactional
    public ApiResponse<CargaProcesoResponse> iniciarCarga(IniciarCargaRequest request) {

        UsuarioEntity operario = obtenerUsuarioAutenticado();

        if (operario == null
                || (!operario.getRol().name().equals("OPERARIO")
                && !operario.getRol().name().equals("ADMINISTRADOR"))) {
            return ApiResponse.error("No tienes permisos para registrar cargas.");
        }

        if (request.getIdCola() == null || request.getPesoLibras() == null
                || request.getPesoLibras().signum() <= 0) {
            return ApiResponse.error("Debes completar correctamente los datos de la carga.");
        }

        ColaEntity cola = colaRepository.findById(request.getIdCola()).orElse(null);

        if (cola == null) {
            return ApiResponse.error("La salida seleccionada no existe.");
        }

        int idProgramacion = cola.getDetalle()
                .getProgramacion()
                .getIdProgramacion();

        ProgramacionEntity programacion = programacionRepository
                .findByIdForUpdate(idProgramacion)
                .orElse(null);

        if (programacion == null) {
            return ApiResponse.error("La programación no existe.");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO
                && programacion.getEstado() != EstadoProgramacion.POR_FINALIZAR) {
            return ApiResponse.error("La programación no permite registrar cargas.");
        }

        CargaEntity cargaExistente = cargaRepository
                .findByCola_IdCola(cola.getIdCola())
                .orElse(null);

        if (cargaExistente != null) {

            if (cargaExistente.getOperario().getIdUsuario() != operario.getIdUsuario()
                    && !operario.getRol().name().equals("ADMINISTRADOR")) {
                return ApiResponse.error("Esta carga pertenece a otro operario.");
            }

            if (cargaExistente.getEstado() == EstadoCarga.REGISTRADA) {
                return ApiResponse.error("Esta salida ya tiene una carga registrada.");
            }

            cargaExistente.setPesoLibras(request.getPesoLibras());

            cargaRepository.saveAndFlush(cargaExistente);

            return ApiResponse.success(
                    convertirAProcesoResponse(cargaExistente),
                    "Peso de la carga actualizado correctamente."
            );
        }

        if (cola.isEstado()) {
            cola.setEstado(false);
            colaRepository.save(cola);
        }

        CargaEntity carga = CargaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .pesoLibras(request.getPesoLibras())
                .programacion(programacion)
                .operario(operario)
                .transportista(cola.getDetalle().getTransportista())
                .cola(cola)
                .estado(EstadoCarga.EN_PROCESO)
                .build();

        cargaRepository.saveAndFlush(carga);

        return ApiResponse.success(
                convertirAProcesoResponse(carga),
                "Carga iniciada correctamente."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CargaProcesoResponse> consultarCargaPorCola(int idCola) {

        UsuarioEntity operario = obtenerUsuarioAutenticado();

        if (operario == null
                || (!operario.getRol().name().equals("OPERARIO")
                && !operario.getRol().name().equals("ADMINISTRADOR"))) {
            return ApiResponse.error("No tienes permisos para consultar esta carga.");
        }

        CargaEntity carga = cargaRepository.findByCola_IdCola(idCola)
                .orElse(null);

        if (carga == null) {
            return ApiResponse.<CargaProcesoResponse>success(
                    null,
                    "La salida todavía no tiene una carga iniciada."
            );
        }

        if (carga.getOperario().getIdUsuario() != operario.getIdUsuario()
                && !operario.getRol().name().equals("ADMINISTRADOR")) {
            return ApiResponse.error("Esta carga pertenece a otro operario.");
        }

        return ApiResponse.success(
                convertirAProcesoResponse(carga),
                "Carga obtenida correctamente."
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> finalizarCarga(FinalizarCargaRequest request) {

        UsuarioEntity operario = obtenerUsuarioAutenticado();

        if (operario == null
                || (!operario.getRol().name().equals("OPERARIO")
                && !operario.getRol().name().equals("ADMINISTRADOR"))) {
            return ApiResponse.error("No tienes permisos para finalizar cargas.");
        }

        if (request.getIdCarga() == null
                || request.getArticulos() == null
                || request.getArticulos().isEmpty()) {
            return ApiResponse.error("Debes registrar al menos un artículo recuperado.");
        }

        for (ArticuloRecuperadoRequest articulo : request.getArticulos()) {

            if (articulo == null
                    || articulo.getArticulo() == null
                    || articulo.getArticulo().isBlank()) {
                return ApiResponse.error("El nombre del artículo es obligatorio.");
            }

            if (articulo.getCantidad() == null || articulo.getCantidad() <= 0) {
                return ApiResponse.error("La cantidad de cada artículo debe ser mayor que cero.");
            }
        }

        CargaEntity carga = cargaRepository.findById(request.getIdCarga())
                .orElse(null);

        if (carga == null) {
            return ApiResponse.error("La carga seleccionada no existe.");
        }

        if (carga.getOperario().getIdUsuario() != operario.getIdUsuario()
                && !operario.getRol().name().equals("ADMINISTRADOR")) {
            return ApiResponse.error("Esta carga pertenece a otro operario.");
        }

        if (carga.getEstado() == EstadoCarga.REGISTRADA) {
            return ApiResponse.error("Esta carga ya fue registrada.");
        }

        int idProgramacion = carga.getProgramacion().getIdProgramacion();

        ProgramacionEntity programacion = programacionRepository
                .findByIdForUpdate(idProgramacion)
                .orElse(null);

        if (programacion == null) {
            return ApiResponse.error("La programación no existe.");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO
                && programacion.getEstado() != EstadoProgramacion.POR_FINALIZAR) {
            return ApiResponse.error("La programación no permite finalizar cargas.");
        }

        if (!articuloRecuperadoRepository
                .findByCarga_IdCarga(carga.getIdCarga()).isEmpty()) {
            return ApiResponse.error("La carga ya contiene artículos registrados.");
        }

        List<ArticuloRecuperadoEntity> articulos = request.getArticulos()
                .stream()
                .map(articulo -> ArticuloRecuperadoEntity.builder()
                        .articulo(articulo.getArticulo().trim())
                        .cantidad(articulo.getCantidad())
                        .carga(carga)
                        .build())
                .toList();

        articuloRecuperadoRepository.saveAllAndFlush(articulos);

        carga.setEstado(EstadoCarga.REGISTRADA);
        cargaRepository.saveAndFlush(carga);

        finalizacionProgramacionService.verificarYFinalizar(idProgramacion);

        return ApiResponse.success(
                carga.getIdCarga(),
                "Carga y artículos recuperados registrados correctamente."
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> registrarCarga(CargaRequest request) {

        UsuarioEntity operario = obtenerUsuarioAutenticado();

        if (operario == null
                || (!operario.getRol().name().equals("OPERARIO")
                && !operario.getRol().name().equals("ADMINISTRADOR"))) {
            return ApiResponse.error("No tienes permisos para registrar cargas.");
        }

        if (request.getIdCola() == null || request.getPesoLibras() == null) {
            return ApiResponse.error("Debes completar los datos de la carga.");
        }

        if (request.getPesoLibras().signum() <= 0) {
            return ApiResponse.error("El peso debe ser mayor que cero.");
        }

        if (request.getArticulos() == null || request.getArticulos().isEmpty()) {
            return ApiResponse.error("Debes registrar al menos un artículo recuperado.");
        }

        for (ArticuloRecuperadoRequest articulo : request.getArticulos()) {

            if (articulo == null
                    || articulo.getArticulo() == null
                    || articulo.getArticulo().isBlank()) {
                return ApiResponse.error("El nombre del artículo es obligatorio.");
            }

            if (articulo.getCantidad() == null || articulo.getCantidad() <= 0) {
                return ApiResponse.error("La cantidad de cada artículo debe ser mayor que cero.");
            }
        }

        ColaEntity cola = colaRepository.findById(request.getIdCola()).orElse(null);

        if (cola == null) {
            return ApiResponse.error("La salida seleccionada no existe.");
        }

        int idProgramacion = cola.getDetalle()
                .getProgramacion()
                .getIdProgramacion();

        ProgramacionEntity programacion = programacionRepository
                .findByIdForUpdate(idProgramacion)
                .orElse(null);

        if (programacion == null) {
            return ApiResponse.error("La programación no existe.");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO
                && programacion.getEstado() != EstadoProgramacion.POR_FINALIZAR) {
            return ApiResponse.error("La programación no permite registrar nuevas cargas.");
        }

        if (cola.isEstado()) {
            return ApiResponse.error("Primero debes recepcionar la salida.");
        }

        if (cargaRepository.existsByCola_IdCola(cola.getIdCola())) {
            return ApiResponse.error("Esta salida ya tiene una carga registrada.");
        }

        UsuarioEntity transportista = cola.getDetalle().getTransportista();

        CargaEntity carga = CargaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .pesoLibras(request.getPesoLibras())
                .programacion(programacion)
                .operario(operario)
                .transportista(transportista)
                .cola(cola)
                .build();

        cargaRepository.saveAndFlush(carga);

        List<ArticuloRecuperadoEntity> articulos = request.getArticulos()
                .stream()
                .map(articulo -> ArticuloRecuperadoEntity.builder()
                        .articulo(articulo.getArticulo().trim())
                        .cantidad(articulo.getCantidad())
                        .carga(carga)
                        .build())
                .toList();

        articuloRecuperadoRepository.saveAllAndFlush(articulos);

        if (programacion.getEstado() == EstadoProgramacion.POR_FINALIZAR) {

            long totalSalidas = colaRepository
                    .countByDetalle_Programacion_IdProgramacion(idProgramacion);

            long totalCargas = cargaRepository
                    .countByProgramacion_IdProgramacion(idProgramacion);

            if (totalSalidas == totalCargas) {

                programacion.setEstado(EstadoProgramacion.FINALIZADO);
                programacion.setFechaFinalizacion(LocalDateTime.now());

                liberacionRecursosService.liberarRecursos(idProgramacion);

                programacionRepository.save(programacion);
            }
        }

        return ApiResponse.success(
                carga.getIdCarga(),
                "Carga y artículos recuperados registrados correctamente."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CargaResponse>> listarCargasPorProgramacion(
            int idProgramacion
    ) {

        UsuarioEntity usuario = obtenerUsuarioAutenticado();

        if (usuario == null) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado.");
        }

        ProgramacionEntity programacion = programacionRepository
                .findById(idProgramacion)
                .orElse(null);

        if (programacion == null) {
            return ApiResponse.error("La programación no existe.");
        }

        String rol = usuario.getRol().name();

        boolean autorizado = rol.equals("ADMINISTRADOR")
                || rol.equals("OPERARIO")
                || (rol.equals("JEFE_UNIDAD")
                && programacion.getJefeUnidad().getIdUsuario() == usuario.getIdUsuario());

        if (!autorizado) {
            return ApiResponse.error("No tienes permisos para consultar estas cargas.");
        }

        List<CargaResponse> cargas = cargaRepository
                .findByProgramacion_IdProgramacionOrderByFechaRegistroDesc(idProgramacion)
                .stream()
                .map(this::convertirAResponse)
                .toList();

        return ApiResponse.success(
                cargas,
                "Cargas obtenidas correctamente."
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalPaginasRendimiento(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Pageable pageable = PageRequest.of(0, 1);
        Page<CantidadCargasResponse> rendimientoPage = cargaRepository.obtenerRendimientoGeneralCargasPaginado(fechaInicio, fechaFin, pageable);

        long totalRegistros = rendimientoPage.getTotalElements();
        int totalPaginas = (int) Math.ceil((double) totalRegistros / Initialize.TAMANIO_PAGINA);

        return ApiResponse.success(totalPaginas, "Total de páginas de rendimiento calculadas correctamente");
    }

    @Override
    public ApiResponse<List<CantidadCargasResponse>> obtenerRendimientoCargasPaginado(
            LocalDateTime fechaInicio, LocalDateTime fechaFin, int pagina) {

        Pageable pageable = PageRequest.of(pagina, Initialize.TAMANIO_PAGINA);
        Page<CantidadCargasResponse> rendimientoPage = cargaRepository.obtenerRendimientoGeneralCargasPaginado(fechaInicio, fechaFin, pageable);

        return ApiResponse.success(rendimientoPage.getContent(), "Reporte de rendimiento paginado obtenido con éxito");
    }

    private UsuarioEntity obtenerUsuarioAutenticado() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            return null;
        }

        return usuarioDetails.getUsuario();
    }


    private CargaResponse convertirAResponse(CargaEntity carga) {

        return CargaResponse.builder()
                .idCarga(carga.getIdCarga())
                .idCola(carga.getCola().getIdCola())
                .fechaRegistro(carga.getFechaRegistro())
                .pesoLibras(carga.getPesoLibras())
                .estado(carga.getEstado().name())
                .idProgramacion(carga.getProgramacion().getIdProgramacion())
                .idTransportista(carga.getTransportista().getIdUsuario())
                .nombreTransportista(carga.getTransportista().getNombre())
                .apellidoTransportista(carga.getTransportista().getApellido())
                .idOperario(carga.getOperario().getIdUsuario())
                .nombreOperario(carga.getOperario().getNombre())
                .apellidoOperario(carga.getOperario().getApellido())
                .build();
    }

    private CargaProcesoResponse convertirAProcesoResponse(CargaEntity carga) {

        return CargaProcesoResponse.builder()
                .idCarga(carga.getIdCarga())
                .idCola(carga.getCola().getIdCola())
                .idProgramacion(carga.getProgramacion().getIdProgramacion())
                .pesoLibras(carga.getPesoLibras())
                .fechaRegistro(carga.getFechaRegistro())
                .build();
    }
}