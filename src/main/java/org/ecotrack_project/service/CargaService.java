package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.CargaRequest;
import org.ecotrack_project.dto.request.FinalizarCargaRequest;
import org.ecotrack_project.dto.request.IniciarCargaRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.CantidadCargasResponse;
import org.ecotrack_project.dto.response.CargaProcesoResponse;
import org.ecotrack_project.dto.response.CargaResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface CargaService {

    ApiResponse<CargaProcesoResponse> iniciarCarga(IniciarCargaRequest request);
    ApiResponse<CargaProcesoResponse> consultarCargaPorCola(int idCola);
    ApiResponse<Integer> finalizarCarga(FinalizarCargaRequest request);
    ApiResponse<Integer> registrarCarga(CargaRequest request);
    ApiResponse<List<CargaResponse>> listarCargasPorProgramacion(int idProgramacion);

    ApiResponse<Integer> obtenerTotalPaginasRendimiento(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    ApiResponse<List<CantidadCargasResponse>> obtenerRendimientoCargasPaginado(LocalDateTime fechaInicio, LocalDateTime fechaFin, int pagina);
}