package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.ProgramacionRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ProgramacionResponse;
import org.ecotrack_project.dto.response.UnidadProgramacionResponse;
import org.ecotrack_project.enums.TipoVehiculo;

import java.time.LocalDate;
import java.util.List;

public interface ProgramacionService {

    ApiResponse<Integer> registrarProgramacion(ProgramacionRequest request);
    ApiResponse<Integer> obtenerTotalProgramacionesPorFecha(LocalDate fecha);
    ApiResponse<Integer> obtenerTotalPaginasProgramacionesPorFecha(LocalDate fecha);
    ApiResponse<List<ProgramacionResponse>> listarProgramacionesPorFecha(LocalDate fecha, int pagina);
    ApiResponse<ProgramacionResponse> obtenerDetalleProgramacion(int idProgramacion);

    ApiResponse<Integer> obtenerTotalMisProgramaciones();
    ApiResponse<Integer> obtenerTotalPaginasMisProgramaciones();
    ApiResponse<List<ProgramacionResponse>> listarMisProgramaciones(int pagina);

    ApiResponse<List<UnidadProgramacionResponse>> listarUnidadesProgramacion(int idProgramacion);
    ApiResponse<UnidadProgramacionResponse> agregarUnidadProgramacion(int idProgramacion, TipoVehiculo tipo);
    ApiResponse<Integer> eliminarUnidadProgramacion(int idProgramacion, int idDetalle);
    ApiResponse<Integer> ejecutarProgramacion(int idProgramacion);
    ApiResponse<ProgramacionResponse> obtenerDetalleMiProgramacion(int idProgramacion);
}