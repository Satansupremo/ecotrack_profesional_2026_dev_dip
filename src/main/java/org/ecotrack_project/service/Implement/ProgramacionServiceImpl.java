
package org.ecotrack_project.service.Implement;

import jakarta.transaction.Transactional;
import org.ecotrack_project.dto.request.ProgramacionRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ProgramacionResponse;
import org.ecotrack_project.dto.response.UnidadProgramacionResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.*;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.security.UsuarioDetails;
import org.ecotrack_project.service.ProgramacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProgramacionServiceImpl implements ProgramacionService {

    private final ProgramacionRepository programacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecomendacionRepository recomendacionRepository;
    private final NotificacionRepository notificacionRepository;
    private final DetalleRepository detalleRepository;
    private final VehiculoRepository vehiculoRepository;

    public ProgramacionServiceImpl(
            ProgramacionRepository programacionRepository,
            SolicitudRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            RecomendacionRepository recomendacionRepository,
            NotificacionRepository notificacionRepository, DetalleRepository detalleRepository, VehiculoRepository vehiculoRepository
    ) {
        this.programacionRepository = programacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.recomendacionRepository = recomendacionRepository;
        this.notificacionRepository = notificacionRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.detalleRepository = detalleRepository;
    }

    @Override
    @Transactional
    public ApiResponse<Integer> registrarProgramacion(ProgramacionRequest request) {

        if (request == null || request.getIdSolicitud() == null ||
                request.getFechaEjecucion() == null) {
            return ApiResponse.error("Los datos de la programación son obligatorios");
        }

        LocalDate fechaEjecucion = request.getFechaEjecucion();

        if (fechaEjecucion.getDayOfWeek() == DayOfWeek.SATURDAY ||
                fechaEjecucion.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return ApiResponse.error(
                    "La fecha de ejecución debe corresponder a un día laborable de lunes a viernes"
            );
        }

        Optional<UsuarioEntity> usuarioAutenticado = obtenerUsuarioAutenticado();

        if (usuarioAutenticado.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity coordinador = usuarioAutenticado.get();

        if (coordinador.getRol() != Rol.COORDINADOR &&
                coordinador.getRol() != Rol.ADMINISTRADOR) {
            return ApiResponse.error("No tiene permisos para registrar programaciones");
        }

        Optional<SolicitudEntity> solicitudOptional =
                solicitudRepository.findByIdForUpdate(request.getIdSolicitud());

        if (solicitudOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity solicitud = solicitudOptional.get();

        if (solicitud.getEstado() != EstadoSolicitud.PAGO_TOTAL_CONFIRMADO) {
            return ApiResponse.error(
                    "La solicitud no está confirmada para programar el recojo"
            );
        }

        if (programacionRepository.existsBySolicitud_IdSolicitud(
                request.getIdSolicitud())) {
            return ApiResponse.error("La solicitud ya tiene una programación registrada");
        }

        Optional<UsuarioEntity> jefeUnidadOptional =
                seleccionarJefeUnidad(fechaEjecucion);

        if (jefeUnidadOptional.isEmpty()) {
            return ApiResponse.error(
                    "No existen jefes de unidad activos para asignar la programación"
            );
        }

        ProgramacionEntity programacion = ProgramacionEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .fechaEjecucion(fechaEjecucion.atStartOfDay())
                .solicitud(solicitud)
                .coordinador(coordinador)
                .jefeUnidad(jefeUnidadOptional.get())
                .estado(EstadoProgramacion.ASIGNADO)
                .build();

        ProgramacionEntity programacionGuardada =
                programacionRepository.save(programacion);

        solicitud.setEstado(EstadoSolicitud.PROGRAMADO);
        solicitudRepository.save(solicitud);

        String mensaje = "¡Buenas noticias! Tu solicitud N.° " +
                solicitud.getIdSolicitud() +
                " ya tiene una fecha programada para el " +
                fechaEjecucion.format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ) +
                ". Te mantendremos informado ante cualquier novedad relacionada con el recojo.";

        NotificacionEntity notificacion = NotificacionEntity.builder()
                .descripcion(mensaje)
                .estado(false)
                .usuario(solicitud.getUsuario())
                .build();

        notificacionRepository.save(notificacion);

        return ApiResponse.success(
                programacionGuardada.getIdProgramacion(),
                "Programación registrada correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> obtenerTotalProgramacionesPorFecha(LocalDate fecha) {

        if (fecha == null) {
            return ApiResponse.error("La fecha de ejecución es obligatoria");
        }

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        long total = programacionRepository
                .countByFechaEjecucionGreaterThanEqualAndFechaEjecucionLessThan(
                        inicio, fin
                );

        return ApiResponse.success(
                (int) total,
                "Total de programaciones obtenido correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> obtenerTotalPaginasProgramacionesPorFecha(LocalDate fecha) {

        if (fecha == null) {
            return ApiResponse.error("La fecha de ejecución es obligatoria");
        }

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        long total = programacionRepository
                .countByFechaEjecucionGreaterThanEqualAndFechaEjecucionLessThan(
                        inicio, fin
                );

        int paginas = (int) Math.ceil(
                (double) total / Initialize.TAMANIO_PAGINA
        );

        return ApiResponse.success(
                paginas,
                "Total de páginas obtenido correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<List<ProgramacionResponse>> listarProgramacionesPorFecha(
            LocalDate fecha,
            int pagina
    ) {

        if (fecha == null) {
            return ApiResponse.error("La fecha de ejecución es obligatoria");
        }

        if (pagina < 0) {
            return ApiResponse.error("El número de página no es válido");
        }

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(
                pagina,
                Initialize.TAMANIO_PAGINA,
                Sort.by(
                        Sort.Order.asc("fechaRegistro"),
                        Sort.Order.asc("idProgramacion")
                )
        );

        Page<ProgramacionEntity> programaciones =
                programacionRepository
                        .findByFechaEjecucionGreaterThanEqualAndFechaEjecucionLessThan(
                                inicio, fin, pageable
                        );

        List<ProgramacionResponse> respuestas = programaciones
                .getContent()
                .stream()
                .map(this::convertirAResponse)
                .toList();

        return ApiResponse.success(
                respuestas,
                "Programaciones obtenidas correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<ProgramacionResponse> obtenerDetalleProgramacion(
            int idProgramacion
    ) {

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findById(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionResponse response =
                convertirAResponse(programacionOptional.get());

        return ApiResponse.success(
                response,
                "Detalle de programación obtenido correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> obtenerTotalMisProgramaciones() {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity usuario = usuarioOptional.get();
        long total;

        if (usuario.getRol() == Rol.JEFE_UNIDAD) {
            total = programacionRepository.countByJefeUnidad_IdUsuario(
                    usuario.getIdUsuario()
            );
        } else if (usuario.getRol() == Rol.ADMINISTRADOR) {
            total = programacionRepository.count();
        } else {
            return ApiResponse.error(
                    "No tiene permisos para consultar las programaciones"
            );
        }

        return ApiResponse.success(
                (int) total,
                "Total de programaciones obtenido correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> obtenerTotalPaginasMisProgramaciones() {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity usuario = usuarioOptional.get();
        long total;

        if (usuario.getRol() == Rol.JEFE_UNIDAD) {
            total = programacionRepository.countByJefeUnidad_IdUsuario(
                    usuario.getIdUsuario()
            );
        } else if (usuario.getRol() == Rol.ADMINISTRADOR) {
            total = programacionRepository.count();
        } else {
            return ApiResponse.error(
                    "No tiene permisos para consultar las programaciones"
            );
        }

        int paginas = (int) Math.ceil(
                (double) total / Initialize.TAMANIO_PAGINA
        );

        return ApiResponse.success(
                paginas,
                "Total de páginas obtenido correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<List<ProgramacionResponse>> listarMisProgramaciones(int pagina) {

        if (pagina < 0) {
            return ApiResponse.error("El número de página no es válido");
        }

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        if (usuario.getRol() != Rol.JEFE_UNIDAD &&
                usuario.getRol() != Rol.ADMINISTRADOR) {
            return ApiResponse.error(
                    "No tiene permisos para consultar las programaciones"
            );
        }

        Pageable pageable = PageRequest.of(
                pagina,
                Initialize.TAMANIO_PAGINA,
                Sort.by(
                        Sort.Order.asc("fechaEjecucion"),
                        Sort.Order.asc("idProgramacion")
                )
        );

        Page<ProgramacionEntity> programaciones;

        if (usuario.getRol() == Rol.JEFE_UNIDAD) {
            programaciones = programacionRepository
                    .findByJefeUnidad_IdUsuario(
                            usuario.getIdUsuario(),
                            pageable
                    );
        } else {
            programaciones = programacionRepository.findAll(pageable);
        }

        List<ProgramacionResponse> respuestas = programaciones
                .getContent()
                .stream()
                .map(this::convertirAResponse)
                .toList();

        return ApiResponse.success(
                respuestas,
                "Programaciones obtenidas correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<List<UnidadProgramacionResponse>> listarUnidadesProgramacion(
            int idProgramacion
    ) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findById(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();
        UsuarioEntity usuario = usuarioOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para consultar esta programación");
        }

        List<UnidadProgramacionResponse> unidades = detalleRepository
                .findByProgramacion_IdProgramacionAndEstadoTrueOrderByIdDetalleAsc(idProgramacion)
                .stream()
                .map(this::convertirAUnidadResponse)
                .toList();

        return ApiResponse.success(
                unidades,
                "Unidades de programación obtenidas correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<UnidadProgramacionResponse> agregarUnidadProgramacion(
            int idProgramacion,
            TipoVehiculo tipo
    ) {

        if (tipo == null) {
            return ApiResponse.error("Debe seleccionar un tipo de vehículo");
        }

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();
        UsuarioEntity usuario = usuarioOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para gestionar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.ASIGNADO) {
            return ApiResponse.error(
                    "No se pueden agregar unidades a una programación que ya fue ejecutada"
            );
        }

        VehiculoEntity vehiculo;

        try {
            vehiculo = seleccionarVehiculoDisponible(tipo);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }

        UsuarioEntity transportista;

        try {
            transportista = seleccionarTransportistaDisponible();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }

        DetalleEntity detalle = DetalleEntity.builder()
                .transportista(transportista)
                .vehiculo(vehiculo)
                .programacion(programacion)
                .estado(true)
                .build();

        transportista.setSituacion(SituacionUsuario.ASIGNADO);
        vehiculo.setSituacion(SituacionVehiculo.ASIGNADO);

        usuarioRepository.save(transportista);
        vehiculoRepository.save(vehiculo);

        DetalleEntity detalleGuardado = detalleRepository.save(detalle);

        return ApiResponse.success(
                convertirAUnidadResponse(detalleGuardado),
                "Unidad agregada correctamente a la programación"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> eliminarUnidadProgramacion(
            int idProgramacion,
            int idDetalle
    ) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();
        UsuarioEntity usuario = usuarioOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para gestionar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.ASIGNADO) {
            return ApiResponse.error(
                    "No se pueden eliminar unidades de una programación que ya fue ejecutada"
            );
        }

        Optional<DetalleEntity> detalleOptional =
                detalleRepository.findByIdDetalleAndProgramacion_IdProgramacionAndEstadoTrue(
                        idDetalle,
                        idProgramacion
                );

        if (detalleOptional.isEmpty()) {
            return ApiResponse.error(
                    "La unidad seleccionada no pertenece a esta programación o ya fue eliminada"
            );
        }

        DetalleEntity detalle = detalleOptional.get();

        UsuarioEntity transportista = detalle.getTransportista();
        VehiculoEntity vehiculo = detalle.getVehiculo();

        transportista.setSituacion(SituacionUsuario.DISPONIBLE);
        vehiculo.setSituacion(SituacionVehiculo.DISPONIBLE);

        usuarioRepository.save(transportista);
        vehiculoRepository.save(vehiculo);

        detalle.setEstado(false);
        detalleRepository.save(detalle);

        return ApiResponse.success(
                idDetalle,
                "Unidad eliminada correctamente de la programación"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> ejecutarProgramacion(int idProgramacion) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();
        UsuarioEntity usuario = usuarioOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para ejecutar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.ASIGNADO) {
            return ApiResponse.error("La programación ya fue ejecutada o no está disponible");
        }

        boolean tieneUnidades = detalleRepository
                .existsByProgramacion_IdProgramacionAndEstadoTrue(idProgramacion);

        if (!tieneUnidades) {
            return ApiResponse.error(
                    "Debe agregar al menos una unidad antes de ejecutar la programación"
            );
        }

        programacion.setEstado(EstadoProgramacion.EJECUTANDO);

        programacionRepository.save(programacion);

        return ApiResponse.success(
                idProgramacion,
                "Programación ejecutada correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<ProgramacionResponse> obtenerDetalleMiProgramacion(int idProgramacion) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findById(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        UsuarioEntity usuario = usuarioOptional.get();
        ProgramacionEntity programacion = programacionOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para consultar esta programación");
        }

        ProgramacionResponse response = convertirAResponse(programacion);

        return ApiResponse.success(
                response,
                "Detalle de programación obtenido correctamente"
        );
    }


    private Optional<UsuarioEntity> seleccionarJefeUnidad(LocalDate fechaEjecucion) {

        List<UsuarioEntity> jefes = usuarioRepository.findByRolAndEstadoTrue(
                Rol.JEFE_UNIDAD
        );

        if (jefes.isEmpty()) {
            return Optional.empty();
        }

        LocalDate inicioSemana = fechaEjecucion.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDate finSemana = inicioSemana.plusDays(5);

        LocalDateTime inicioSemanaDateTime = inicioSemana.atStartOfDay();
        LocalDateTime finSemanaDateTime = finSemana.atStartOfDay();

        LocalDateTime inicioDia = fechaEjecucion.atStartOfDay();
        LocalDateTime finDia = fechaEjecucion.plusDays(1).atStartOfDay();

        Map<Integer, CargaJefeUnidad> cargas = new HashMap<>();

        for (UsuarioEntity jefe : jefes) {

            long unidadesSemana =
                    programacionRepository.sumarUnidadesRecomendadasPorJefeYPeriodo(
                            jefe.getIdUsuario(),
                            inicioSemanaDateTime,
                            finSemanaDateTime
                    );

            long unidadesDia =
                    programacionRepository.sumarUnidadesRecomendadasPorJefeYPeriodo(
                            jefe.getIdUsuario(),
                            inicioDia,
                            finDia
                    );

            LocalDateTime ultimaAsignacion =
                    programacionRepository
                            .findFirstByJefeUnidad_IdUsuarioOrderByFechaRegistroDescIdProgramacionDesc(
                                    jefe.getIdUsuario()
                            )
                            .map(ProgramacionEntity::getFechaRegistro)
                            .orElse(null);

            cargas.put(
                    jefe.getIdUsuario(),
                    new CargaJefeUnidad(
                            unidadesSemana,
                            unidadesDia,
                            ultimaAsignacion
                    )
            );
        }

        return jefes.stream()
                .min(
                        Comparator
                                .comparingLong(
                                        (UsuarioEntity jefe) ->
                                                cargas.get(jefe.getIdUsuario()).unidadesSemana()
                                )
                                .thenComparingLong(
                                        jefe -> cargas.get(jefe.getIdUsuario()).unidadesDia()
                                )
                                .thenComparing(
                                        jefe -> cargas.get(jefe.getIdUsuario()).ultimaAsignacion(),
                                        Comparator.nullsFirst(Comparator.naturalOrder())
                                )
                                .thenComparingInt(UsuarioEntity::getIdUsuario)
                );
    }

    private record CargaJefeUnidad(
            long unidadesSemana,
            long unidadesDia,
            LocalDateTime ultimaAsignacion
    ) {
    }

    private Optional<UsuarioEntity> obtenerUsuarioAutenticado() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            return Optional.empty();
        }

        return Optional.of(usuarioDetails.getUsuario());
    }

    private ProgramacionResponse convertirAResponse(
            ProgramacionEntity programacion
    ) {

        SolicitudEntity solicitud = programacion.getSolicitud();

        Optional<RecomendacionEntity> recomendacionOptional =
                recomendacionRepository.findBySolicitud_IdSolicitud(
                        solicitud.getIdSolicitud()
                );

        RecomendacionEntity recomendacion =
                recomendacionOptional.orElse(null);

        UsuarioEntity jefeUnidad = programacion.getJefeUnidad();

        return ProgramacionResponse.builder()
                .idProgramacion(programacion.getIdProgramacion())
                .fechaRegistro(programacion.getFechaRegistro())
                .fechaEjecucion(
                        programacion.getFechaEjecucion() != null
                                ? programacion.getFechaEjecucion().toLocalDate()
                                : null
                )
                .estado(programacion.getEstado().name())
                .idSolicitud(solicitud.getIdSolicitud())
                .tituloSolicitud(solicitud.getTitulo())
                .descripcionSolicitud(solicitud.getDescripcion())
                .ubicacionSolicitud(solicitud.getUbicacion())
                .fechaRegistroSolicitud(solicitud.getFechaRegistro())
                .cantidadUnidades(
                        recomendacion != null
                                ? recomendacion.getCantidadUnidades()
                                : null
                )
                .razonCantidadUnidades(
                        recomendacion != null
                                ? recomendacion.getRazonCantidadUnidades()
                                : null
                )
                .equipoSeguridad(
                        recomendacion != null
                                ? recomendacion.getEquipoSeguridad()
                                : null
                )
                .otrasRecomendaciones(
                        recomendacion != null
                                ? recomendacion.getOtrasRecomendaciones()
                                : null
                )
                .idJefeUnidad(jefeUnidad.getIdUsuario())
                .nombreJefeUnidad(jefeUnidad.getNombre())
                .apellidoJefeUnidad(jefeUnidad.getApellido())
                .build();
    }

    private VehiculoEntity seleccionarVehiculoDisponible(TipoVehiculo tipo) {

        return vehiculoRepository
                .findFirstByTipoAndEstadoTrueAndSituacionOrderByIdVehiculoAsc(
                        tipo,
                        SituacionVehiculo.DISPONIBLE
                )
                .orElseThrow(() -> new RuntimeException(
                        "No hay vehículos disponibles del tipo seleccionado"
                ));
    }

    private UsuarioEntity seleccionarTransportistaDisponible() {

        return usuarioRepository
                .findFirstByRolAndEstadoTrueAndSituacionOrderByIdUsuarioAsc(
                        Rol.TRANSPORTISTA,
                        SituacionUsuario.DISPONIBLE
                )
                .orElseThrow(() -> new RuntimeException(
                        "No hay transportistas disponibles"
                ));
    }

    private boolean tienePermisoGestionarProgramacion(
            UsuarioEntity usuario,
            ProgramacionEntity programacion
    ) {

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            return true;
        }

        return usuario.getRol() == Rol.JEFE_UNIDAD
                && programacion.getJefeUnidad().getIdUsuario() == usuario.getIdUsuario();
    }

    private UnidadProgramacionResponse convertirAUnidadResponse(DetalleEntity detalle) {

        UsuarioEntity transportista = detalle.getTransportista();
        VehiculoEntity vehiculo = detalle.getVehiculo();

        return UnidadProgramacionResponse.builder()
                .idDetalle(detalle.getIdDetalle())
                .idTransportista(transportista.getIdUsuario())
                .nombreTransportista(transportista.getNombre())
                .apellidoTransportista(transportista.getApellido())
                .celular(transportista.getCelular())
                .idVehiculo(vehiculo.getIdVehiculo())
                .placa(vehiculo.getPlaca())
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .tipoVehiculo(vehiculo.getTipo().name())
                .capacidadLibras(vehiculo.getCapacidadLibras())
                .build();
    }

}
