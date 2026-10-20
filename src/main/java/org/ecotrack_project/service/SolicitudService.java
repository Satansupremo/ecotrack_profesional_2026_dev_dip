
package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.SolicitudRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SolicitudResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SolicitudService {

    ApiResponse<Integer> obtenerTotalPaginas();

    ApiResponse<Integer> obtenerTotalSolicitudesByIdUsuario();

    ApiResponse<List<SolicitudResponse>> listarSolicitudes(int pagina);

    ApiResponse<Integer> registrarSolicitud(SolicitudRequest solicitudRequest);

    ApiResponse<SolicitudResponse> obtenerSolicitud(int idSolicitud);
}
