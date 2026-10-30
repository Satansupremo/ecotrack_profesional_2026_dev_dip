package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.PagoInicialRequest;
import org.ecotrack_project.dto.request.RechazoPagoInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.PagoInicialDetalleResponse;

public interface PagoInicialService {

    ApiResponse<Integer> registrarPagoInicial(PagoInicialRequest pagoInicialRequest);
    ApiResponse<Integer> registrarPagoInicial(int idSolicitud, PagoInicialRequest request);
    ApiResponse<PagoInicialDetalleResponse> obtenerDetallePagoInicial(int idSolicitud);
    ApiResponse<Integer> confirmarPagoInicial(int idSolicitud);
    ApiResponse<Integer> rechazarPagoInicial(int idSolicitud, RechazoPagoInicialRequest request);
}