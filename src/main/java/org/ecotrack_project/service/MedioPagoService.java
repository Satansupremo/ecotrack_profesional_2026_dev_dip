package org.ecotrack_project.service;

import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.MedioPagoResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MedioPagoService {

    ApiResponse<List<MedioPagoResponse>> listarMediosPago();
}
