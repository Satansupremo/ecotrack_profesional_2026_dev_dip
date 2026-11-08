package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.CargaRequest;
import org.ecotrack_project.dto.request.FinalizarCargaRequest;
import org.ecotrack_project.dto.request.IniciarCargaRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.CargaProcesoResponse;
import org.ecotrack_project.dto.response.CargaResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CargaService {

    ApiResponse<CargaProcesoResponse> iniciarCarga(IniciarCargaRequest request);
    ApiResponse<CargaProcesoResponse> consultarCargaPorCola(int idCola);
    ApiResponse<Integer> finalizarCarga(FinalizarCargaRequest request);
    ApiResponse<Integer> registrarCarga(CargaRequest request);
    ApiResponse<List<CargaResponse>> listarCargasPorProgramacion(int idProgramacion);
}