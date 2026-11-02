
package org.ecotrack_project.service;

import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ReservaUnidadResponse;
import org.ecotrack_project.enums.TipoVehiculo;

import java.util.List;

public interface ReservaUnidadService {

    ApiResponse<ReservaUnidadResponse> reservarUnidadEmergencia(int idProgramacion, TipoVehiculo tipo);
    ApiResponse<List<ReservaUnidadResponse>> listarReservasEmergencia(int idProgramacion);
    ApiResponse<Integer> liberarReservaEmergencia(int idProgramacion, int idReserva);
    ApiResponse<Integer> confirmarUnidadesEmergencia(int idProgramacion);
    ApiResponse<Integer> cancelarReservasEmergencia(int idProgramacion);
}
