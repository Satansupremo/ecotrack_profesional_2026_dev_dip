package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.AsignacionTotalRequest;
import org.ecotrack_project.dto.request.RecomendacionRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.AsignacionTotalResponse;
import org.springframework.stereotype.Service;

@Service
public interface AsignacionTotalService {

    ApiResponse<Integer> registrarAsignacionTotal(int idSolicitud, AsignacionTotalRequest asignacionRequest, RecomendacionRequest recomendacionRequest);
    ApiResponse<AsignacionTotalResponse> obtenerAsignacionTotal(int idSolicitud);
    ApiResponse<Void> verificarAsignacionTotal(int idSolicitud);
}