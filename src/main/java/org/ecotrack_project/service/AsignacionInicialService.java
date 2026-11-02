package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.AsignacionInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.AsignacionInicialResponse;

public interface AsignacionInicialService {

    ApiResponse<Integer> registrarAsignacionInicial(int idSolicitud, AsignacionInicialRequest request);
    ApiResponse<Boolean> validarAsignacionInicial(int idSolicitud);
    ApiResponse<AsignacionInicialResponse> obtenerAsignacionInicial(int idSolicitud);
}