package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.MedioPagoRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.MedioPagoResponse;

import java.util.List;

public interface MedioPagoService {

    ApiResponse<List<MedioPagoResponse>> listarMediosPago();
    ApiResponse<Integer> obtenerTotalPaginas();
    ApiResponse<List<MedioPagoResponse>> listarMediosPago(int pagina);
    ApiResponse<MedioPagoResponse> obtenerMedioPagoPorId(int id);
    ApiResponse<Integer> registrarMedioPago(MedioPagoRequest request);
    ApiResponse<Integer> actualizarMedioPago(int id, MedioPagoRequest request);
    ApiResponse<String> cambiarEstadoMedioPago(int id, boolean nuevoEstado);
}
