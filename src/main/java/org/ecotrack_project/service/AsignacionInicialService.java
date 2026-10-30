package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.AsignacionInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;

public interface AsignacionInicialService {

    ApiResponse<Integer> registrarAsignacionInicial(int idSolicitud, AsignacionInicialRequest request);
}