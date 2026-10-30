package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.PagoInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;

public interface PagoInicialService {

    ApiResponse<Integer> registrarPagoInicial(PagoInicialRequest pagoInicialRequest);

}