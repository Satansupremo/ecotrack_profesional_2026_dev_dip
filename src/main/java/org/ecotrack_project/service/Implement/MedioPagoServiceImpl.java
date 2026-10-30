package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.MedioPagoResponse;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.ecotrack_project.service.MedioPagoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedioPagoServiceImpl implements MedioPagoService {

    private final MedioPagoRepository medioPagoRepository;

    @Override
    public ApiResponse<List<MedioPagoResponse>> listarMediosPago() {

        List<MedioPagoResponse> response = medioPagoRepository.findByEstadoTrue()
                .stream()
                .map(entity -> MedioPagoResponse.builder()
                        .idMedioPago(entity.getIdMedioPago())
                        .nombre(entity.getNombre())
                        .descripcion(entity.getDescripcion())
                        .numeroCuenta(entity.getNumeroCuenta())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(
                response,
                "Lista de medios de pago obtenida correctamente"
        );
    }


}
