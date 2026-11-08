package org.ecotrack_project.service;

import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ColaResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ColaService {

    ApiResponse<Integer> recepcionarSalida(int idCola);
    ApiResponse<List<ColaResponse>> listarSalidasPorProgramacion(int idProgramacion);
}