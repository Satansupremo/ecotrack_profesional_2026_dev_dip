package org.ecotrack_project.service;

import org.ecotrack_project.dto.response.ApiResponse;

public interface AsignacionCoordinadorService {

    ApiResponse<Integer> tomarSolicitud(int idSolicitud);
    ApiResponse<Void> cancelarSeleccion(int idSolicitud);
}