package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ColaResponse;
import org.ecotrack_project.entity.ColaEntity;
import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.repository.ColaRepository;
import org.ecotrack_project.repository.ProgramacionRepository;
import org.ecotrack_project.service.ColaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ColaServiceImpl implements ColaService {

    private final ColaRepository colaRepository;
    private final ProgramacionRepository programacionRepository;
    private final FinalizacionProgramacionService finalizacionProgramacionService;

    public ColaServiceImpl(
            ColaRepository colaRepository,
            ProgramacionRepository programacionRepository,
            FinalizacionProgramacionService finalizacionProgramacionService
    ) {
        this.colaRepository = colaRepository;
        this.programacionRepository = programacionRepository;
        this.finalizacionProgramacionService = finalizacionProgramacionService;
    }

    @Override
    @Transactional
    public ApiResponse<Integer> recepcionarSalida(int idCola) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        boolean autorizado = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_OPERARIO")
                                || authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                );

        if (!autorizado) {
            return ApiResponse.error(
                    "No tiene permisos para recepcionar salidas"
            );
        }

        Optional<ColaEntity> colaOptional = colaRepository.findById(idCola);

        if (colaOptional.isEmpty()) {
            return ApiResponse.error("Salida no encontrada");
        }

        ColaEntity colaInicial = colaOptional.get();

        int idProgramacion = colaInicial.getDetalle()
                .getProgramacion()
                .getIdProgramacion();

        Optional<ProgramacionEntity> programacionOptional =
                programacionRepository.findByIdForUpdate(idProgramacion);

        if (programacionOptional.isEmpty()) {
            return ApiResponse.error("Programación no encontrada");
        }

        ProgramacionEntity programacion = programacionOptional.get();

        if (programacion.getEstado() != EstadoProgramacion.EJECUTANDO
                && programacion.getEstado() != EstadoProgramacion.POR_FINALIZAR) {

            return ApiResponse.error(
                    "No se pueden recepcionar salidas de esta programación"
            );
        }

        Optional<ColaEntity> colaActualOptional =
                colaRepository.findById(idCola);

        if (colaActualOptional.isEmpty()) {
            return ApiResponse.error("Salida no encontrada");
        }

        ColaEntity cola = colaActualOptional.get();

        if (!cola.isEstado()) {
            return ApiResponse.error(
                    "Esta salida ya fue recepcionada"
            );
        }

        cola.setEstado(false);
        colaRepository.saveAndFlush(cola);

        finalizacionProgramacionService.verificarYFinalizar(idProgramacion);

        return ApiResponse.success(
                idCola,
                "Salida recepcionada correctamente"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ColaResponse>> listarSalidasPorProgramacion(
            int idProgramacion
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error(
                    "No se pudo identificar al usuario autenticado."
            );
        }

        boolean autorizado = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_OPERARIO")
                                || authority.getAuthority().equals("ROLE_ADMINISTRADOR")
                );

        if (!autorizado) {
            return ApiResponse.error(
                    "No tienes permisos para consultar las salidas."
            );
        }

        ProgramacionEntity programacion = programacionRepository
                .findById(idProgramacion)
                .orElse(null);

        if (programacion == null) {
            return ApiResponse.error("La programación no existe.");
        }

        List<ColaResponse> salidas = colaRepository
                .findByDetalle_Programacion_IdProgramacionOrderByFechaRegistroAscIdColaAsc(
                        idProgramacion
                )
                .stream()
                .map(cola -> ColaResponse.builder()
                        .idCola(cola.getIdCola())
                        .fechaRegistro(cola.getFechaRegistro())
                        .estado(cola.isEstado())
                        .idProgramacion(idProgramacion)
                        .idDetalle(cola.getDetalle().getIdDetalle())
                        .idTransportista(cola.getDetalle().getTransportista().getIdUsuario())
                        .nombreTransportista(cola.getDetalle().getTransportista().getNombre())
                        .apellidoTransportista(cola.getDetalle().getTransportista().getApellido())
                        .idVehiculo(cola.getDetalle().getVehiculo().getIdVehiculo())
                        .placaVehiculo(cola.getDetalle().getVehiculo().getPlaca())
                        .tipoVehiculo(cola.getDetalle().getVehiculo().getTipo().name())
                        .build())
                .toList();

        return ApiResponse.success(
                salidas,
                "Salidas obtenidas correctamente."
        );
    }
}