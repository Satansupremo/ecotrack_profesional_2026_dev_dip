package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.PagoInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.service.PagoInicialService;
import org.springframework.stereotype.Service;

@Service
public class PagoInicialServiceImpl implements PagoInicialService {
    @Override
    public ApiResponse<Integer> registrarPagoInicial(PagoInicialRequest pagoInicialRequest) {
        return null;
    }
}
