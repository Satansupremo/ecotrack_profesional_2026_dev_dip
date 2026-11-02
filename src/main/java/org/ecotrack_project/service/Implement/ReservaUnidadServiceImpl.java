
package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ReservaUnidadResponse;
import org.ecotrack_project.entity.*;
import org.ecotrack_project.enums.*;
import org.ecotrack_project.repository.*;
import org.ecotrack_project.security.UsuarioDetails;
import org.ecotrack_project.service.ReservaUnidadService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaUnidadServiceImpl implements ReservaUnidadService {

    private static final int MINUTOS_EXPIRACION = 15;

    private final ReservaUnidadRepository reservaUnidadRepository;
    private final ProgramacionRepository programacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final DetalleRepository detalleRepository;

    public ReservaUnidadServiceImpl(
            ReservaUnidadRepository reservaUnidadRepository,
            ProgramacionRepository programacionRepository,
            UsuarioRepository usuarioRepository,
            VehiculoRepository vehiculoRepository,
            DetalleRepository detalleRepository
    ) {
        this.reservaUnidadRepository = reservaUnidadRepository;
        this.programacionRepository = programacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.detalleRepository = detalleRepository;
    }

    @Override
    @Transactional
    public ApiResponse<ReservaUnidadResponse> reservarUnidadEmergencia(
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

        UsuarioEntity usuario = usuarioOptional.get();

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para gestionar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO) {
            return ApiResponse.error(
                    "Solo se pueden reservar unidades para programaciones en ejecución"
            );
        }

        liberarReservasVencidas();

        VehiculoEntity vehiculo = vehiculoRepository
                .buscarVehiculosDisponiblesConBloqueo(
                        tipo,
                        SituacionVehiculo.DISPONIBLE
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (vehiculo == null) {
            return ApiResponse.error(
                    "No hay vehículos disponibles del tipo seleccionado"
            );
        }

        UsuarioEntity transportista = usuarioRepository
                .buscarTransportistasDisponiblesConBloqueo(
                        Rol.TRANSPORTISTA,
                        SituacionUsuario.DISPONIBLE
                )
                .stream()
                .findFirst()
                .orElse(null);

        if (transportista == null) {
            return ApiResponse.error("No hay transportistas disponibles");
        }

        LocalDateTime ahora = LocalDateTime.now();

        vehiculo.setSituacion(SituacionVehiculo.RESERVADO);
        transportista.setSituacion(SituacionUsuario.RESERVADO);

        vehiculoRepository.save(vehiculo);
        usuarioRepository.save(transportista);

        ReservaUnidadEntity reserva = ReservaUnidadEntity.builder()
                .fechaReserva(ahora)
                .fechaExpiracion(ahora.plusMinutes(MINUTOS_EXPIRACION))
                .programacion(programacion)
                .transportista(transportista)
                .vehiculo(vehiculo)
                .usuarioReserva(usuario)
                .build();

        ReservaUnidadEntity reservaGuardada =
                reservaUnidadRepository.save(reserva);

        return ApiResponse.success(
                convertirAResponse(reservaGuardada),
                "Unidad de emergencia reservada correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<List<ReservaUnidadResponse>> listarReservasEmergencia(
            int idProgramacion
    ) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findById(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para consultar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO) {
            return ApiResponse.error(
                    "La programación no se encuentra en ejecución"
            );
        }

        liberarReservasVencidas();

        List<ReservaUnidadResponse> reservas = reservaUnidadRepository
                .findByProgramacion_IdProgramacionOrderByIdReservaAsc(idProgramacion)
                .stream()
                .filter(reserva ->
                        reserva.getUsuarioReserva().getIdUsuario()
                                == usuario.getIdUsuario()
                )
                .map(this::convertirAResponse)
                .toList();

        return ApiResponse.success(
                reservas,
                "Reservas de emergencia obtenidas correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> liberarReservaEmergencia(
            int idProgramacion,
            int idReserva
    ) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para gestionar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO) {
            return ApiResponse.error(
                    "La programación no se encuentra en ejecución"
            );
        }

        Optional<ReservaUnidadEntity> reservaOptional =
                reservaUnidadRepository
                        .findByIdReservaAndUsuarioReserva_IdUsuario(
                                idReserva,
                                usuario.getIdUsuario()
                        );

        if (reservaOptional.isEmpty()) {
            return ApiResponse.error(
                    "La reserva no existe o no pertenece al usuario autenticado"
            );
        }

        ReservaUnidadEntity reserva = reservaOptional.get();

        if (reserva.getProgramacion().getIdProgramacion() != idProgramacion) {
            return ApiResponse.error(
                    "La reserva seleccionada no pertenece a esta programación"
            );
        }

        liberarRecursos(reserva);

        reservaUnidadRepository.delete(reserva);

        return ApiResponse.success(
                idReserva,
                "Reserva de emergencia liberada correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> confirmarUnidadesEmergencia(
            int idProgramacion
    ) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para gestionar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO) {
            return ApiResponse.error(
                    "La programación no se encuentra en ejecución"
            );
        }

        liberarReservasVencidas();

        List<ReservaUnidadEntity> reservas = reservaUnidadRepository
                .findByProgramacion_IdProgramacionOrderByIdReservaAsc(idProgramacion)
                .stream()
                .filter(reserva ->
                        reserva.getUsuarioReserva().getIdUsuario()
                                == usuario.getIdUsuario()
                )
                .toList();

        if (reservas.isEmpty()) {
            return ApiResponse.error(
                    "Debe reservar al menos una unidad de emergencia"
            );
        }

        for (ReservaUnidadEntity reserva : reservas) {

            UsuarioEntity transportista = reserva.getTransportista();
            VehiculoEntity vehiculo = reserva.getVehiculo();

            if (transportista.getSituacion() != SituacionUsuario.RESERVADO ||
                    vehiculo.getSituacion() != SituacionVehiculo.RESERVADO) {
                return ApiResponse.error(
                        "Una de las unidades reservadas ya no está disponible. Actualice la información"
                );
            }

            transportista.setSituacion(SituacionUsuario.ASIGNADO);
            vehiculo.setSituacion(SituacionVehiculo.ASIGNADO);

            DetalleEntity detalle = DetalleEntity.builder()
                    .transportista(transportista)
                    .vehiculo(vehiculo)
                    .programacion(programacion)
                    .estado(true)
                    .build();

            usuarioRepository.save(transportista);
            vehiculoRepository.save(vehiculo);
            detalleRepository.save(detalle);
        }

        int totalConfirmadas = reservas.size();

        reservaUnidadRepository.deleteAll(reservas);

        return ApiResponse.success(
                totalConfirmadas,
                "Se agregaron correctamente " + totalConfirmadas +
                        " unidades de emergencia a la programación"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> cancelarReservasEmergencia(
            int idProgramacion
    ) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("No se pudo identificar al usuario autenticado");
        }

        UsuarioEntity usuario = usuarioOptional.get();

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();

        if (!tienePermisoGestionarProgramacion(usuario, programacion)) {
            return ApiResponse.error("No tiene permisos para gestionar esta programación");
        }

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO) {
            return ApiResponse.error(
                    "La programación no se encuentra en ejecución"
            );
        }

        List<ReservaUnidadEntity> reservas = reservaUnidadRepository
                .findByProgramacion_IdProgramacionOrderByIdReservaAsc(idProgramacion)
                .stream()
                .filter(reserva ->
                        reserva.getUsuarioReserva().getIdUsuario()
                                == usuario.getIdUsuario()
                )
                .toList();

        if (reservas.isEmpty()) {
            return ApiResponse.success(
                    0,
                    "No existen reservas pendientes para liberar"
            );
        }

        reservas.forEach(this::liberarRecursos);

        int totalLiberadas = reservas.size();

        reservaUnidadRepository.deleteAll(reservas);

        return ApiResponse.success(
                totalLiberadas,
                "Reservas de emergencia canceladas correctamente"
        );
    }

    private void liberarReservasVencidas() {

        LocalDateTime ahora = LocalDateTime.now();

        List<ReservaUnidadEntity> reservasVencidas =
                reservaUnidadRepository.findByFechaExpiracionLessThanEqual(ahora);

        for (ReservaUnidadEntity reserva : reservasVencidas) {
            liberarRecursos(reserva);
        }

        if (!reservasVencidas.isEmpty()) {
            reservaUnidadRepository.deleteAll(reservasVencidas);
        }
    }

    private void liberarRecursos(ReservaUnidadEntity reserva) {

        UsuarioEntity transportista = reserva.getTransportista();
        VehiculoEntity vehiculo = reserva.getVehiculo();

        if (transportista.getSituacion() == SituacionUsuario.RESERVADO) {
            transportista.setSituacion(SituacionUsuario.DISPONIBLE);
            usuarioRepository.save(transportista);
        }

        if (vehiculo.getSituacion() == SituacionVehiculo.RESERVADO) {
            vehiculo.setSituacion(SituacionVehiculo.DISPONIBLE);
            vehiculoRepository.save(vehiculo);
        }
    }

    private ReservaUnidadResponse convertirAResponse(
            ReservaUnidadEntity reserva
    ) {

        UsuarioEntity transportista = reserva.getTransportista();
        VehiculoEntity vehiculo = reserva.getVehiculo();

        return ReservaUnidadResponse.builder()
                .idReserva(reserva.getIdReserva())
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
                .fechaReserva(reserva.getFechaReserva())
                .fechaExpiracion(reserva.getFechaExpiracion())
                .build();
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

    private boolean tienePermisoGestionarProgramacion(
            UsuarioEntity usuario,
            ProgramacionEntity programacion
    ) {

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            return true;
        }

        return usuario.getRol() == Rol.JEFE_UNIDAD
                && programacion.getJefeUnidad().getIdUsuario()
                == usuario.getIdUsuario();
    }
}
