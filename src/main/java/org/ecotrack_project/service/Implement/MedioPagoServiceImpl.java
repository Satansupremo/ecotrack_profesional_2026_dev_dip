package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.MedioPagoRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.MedioPagoResponse;
import org.ecotrack_project.entity.MedioPagoEntity;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.MedioPagoRepository;
import org.ecotrack_project.service.MedioPagoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                        .estado(entity.isEstado())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(
                response,
                "Lista de medios de pago obtenida correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalPaginas() {
        long totalRegistros = medioPagoRepository.count();
        int totalPaginas = (int) Math.ceil((double) totalRegistros / Initialize.TAMANIO_PAGINA);

        return ApiResponse.success(totalPaginas, "Total de páginas obtenidas correctamente");
    }

    @Override
    public ApiResponse<List<MedioPagoResponse>> listarMediosPago(int pagina) {
        Pageable pageable = PageRequest.of(pagina, Initialize.TAMANIO_PAGINA);
        Page<MedioPagoEntity> mediosPagoPage = medioPagoRepository.findAll(pageable);

        List<MedioPagoResponse> response = mediosPagoPage.getContent()
                .stream()
                .map(entity -> MedioPagoResponse.builder()
                        .idMedioPago(entity.getIdMedioPago())
                        .nombre(entity.getNombre())
                        .descripcion(entity.getDescripcion())
                        .numeroCuenta(entity.getNumeroCuenta())
                        .estado(entity.isEstado())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(response, "Lista de medios de pago paginada obtenida correctamente");
    }

    @Override
    public ApiResponse<MedioPagoResponse> obtenerMedioPagoPorId(int id) {
        return medioPagoRepository.findById(id)
                .map(entity -> {
                    MedioPagoResponse response = MedioPagoResponse.builder()
                            .idMedioPago(entity.getIdMedioPago())
                            .nombre(entity.getNombre())
                            .descripcion(entity.getDescripcion())
                            .numeroCuenta(entity.getNumeroCuenta())
                            .estado(entity.isEstado())
                            .build();
                    return ApiResponse.success(response, "Medio de pago encontrado correctamente");
                })
                .orElseGet(() -> ApiResponse.error("Medio de pago no encontrado"));
    }

    @Override
    public ApiResponse<Integer> registrarMedioPago(MedioPagoRequest request) {
        MedioPagoEntity entity = MedioPagoEntity.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .numeroCuenta(request.getNumeroCuenta())
                .estado(true)
                .build();

        MedioPagoEntity guardado = medioPagoRepository.save(entity);
        return ApiResponse.success(guardado.getIdMedioPago(), "Medio de pago registrado correctamente");
    }

    @Override
    public ApiResponse<Integer> actualizarMedioPago(int id, MedioPagoRequest request) {
        return medioPagoRepository.findById(id)
                .map(entity -> {
                    entity.setNombre(request.getNombre());
                    entity.setDescripcion(request.getDescripcion());
                    entity.setNumeroCuenta(request.getNumeroCuenta());

                    MedioPagoEntity actualizado = medioPagoRepository.save(entity);
                    return ApiResponse.success(actualizado.getIdMedioPago(), "Medio de pago actualizado correctamente");
                })
                .orElseGet(() -> ApiResponse.error("Medio de pago no encontrado"));
    }

    @Override
    public ApiResponse<String> cambiarEstadoMedioPago(int id, boolean nuevoEstado) {
        return medioPagoRepository.findById(id)
                .map(entity -> {
                    entity.setEstado(nuevoEstado);
                    medioPagoRepository.save(entity);
                    String mensaje = nuevoEstado ? "Medio de pago activado correctamente" : "Medio de pago desactivado correctamente";
                    return ApiResponse.success(mensaje, mensaje);
                })
                .orElseGet(() -> ApiResponse.error("Medio de pago no encontrado"));
    }
}
