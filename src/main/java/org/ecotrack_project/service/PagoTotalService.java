package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.PagoTotalRequest;
import org.ecotrack_project.dto.request.RechazoPagoTotalRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.PagoTotalDetalleResponse;

public interface PagoTotalService {

    ApiResponse<Integer> registrarPagoTotal(int idSolicitud, PagoTotalRequest request);
    ApiResponse<PagoTotalDetalleResponse> obtenerDetallePagoTotal(int idSolicitud);
    ApiResponse<Integer> confirmarPagoTotal(int idSolicitud);
    ApiResponse<Integer> rechazarPagoTotal(int idSolicitud, RechazoPagoTotalRequest request);
}