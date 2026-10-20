package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.VehiculoRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.VehiculoResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VehiculoService {

    ApiResponse<Integer> obtenerTotalPaginas();
    ApiResponse<List<VehiculoResponse>> listarVehiculo(int pagina);
    ApiResponse<Integer> registrarVehiculo(VehiculoRequest vehiculoRequest);
    ApiResponse<Integer> actualizarVehiculo(int idVehiculo, VehiculoRequest vehiculoRequest);
    ApiResponse<VehiculoResponse> obtenerVehiculoById(int idVehiculo);
}
