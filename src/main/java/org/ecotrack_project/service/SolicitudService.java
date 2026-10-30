
package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.SolicitudRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SolicitudResponse;

import java.util.List;

public interface SolicitudService {

    ApiResponse<Integer> obtenerTotalPaginas();
    ApiResponse<Integer> obtenerTotalSolicitudesByIdUsuario();
    ApiResponse<List<SolicitudResponse>> listarSolicitudes(int pagina);
    ApiResponse<Integer> registrarSolicitud(SolicitudRequest solicitudRequest);
    ApiResponse<SolicitudResponse> obtenerSolicitud(int idSolicitud);

    ApiResponse<Integer> obtenerTotalSolicitudesPendientes();
    ApiResponse<Integer> obtenerTotalPaginasPendientes();
    ApiResponse<List<SolicitudResponse>> listarSolicitudesPendientes(int pagina);
    ApiResponse<SolicitudResponse> obtenerSolicitudPendiente(int idSolicitud);

    ApiResponse<Integer> obtenerTotalSolicitudesPIE();
    ApiResponse<Integer> obtenerTotalPaginasPIE();
    ApiResponse<List<SolicitudResponse>> listarSolicitudesPIE(int pagina);
}
