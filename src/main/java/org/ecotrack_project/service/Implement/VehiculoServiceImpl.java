package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.VehiculoRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.VehiculoResponse;
import org.ecotrack_project.entity.VehiculoEntity;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.VehiculoRepository;
import org.ecotrack_project.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehiculoServiceImpl implements VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Override
    public ApiResponse<Integer> obtenerTotalPaginas() {
        long totalRegistros = vehiculoRepository.count();
        int totalPaginas = (int) Math.ceil((double) totalRegistros / Initialize.TAMANIO_PAGINA);

        return ApiResponse.success(totalPaginas, "Total de páginas obtenidas correctamente");
    }

    @Override
    public ApiResponse<List<VehiculoResponse>> listarVehiculo(int pagina) {
        Pageable pageable = PageRequest.of(pagina, Initialize.TAMANIO_PAGINA);
        Page<VehiculoEntity> vehiculosPage = vehiculoRepository.findAll(pageable);

        List<VehiculoResponse> response = vehiculosPage.getContent()
                .stream()
                .map(this::mapearA_Response)
                .collect(Collectors.toList());

        return ApiResponse.success(response, "Lista obtenida correctamente");
    }

    @Override
    public ApiResponse<Integer> registrarVehiculo(VehiculoRequest vehiculoRequest) {
        VehiculoEntity entity = VehiculoEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .placa(vehiculoRequest.getPlaca())
                .marca(vehiculoRequest.getMarca())
                .modelo(vehiculoRequest.getModelo())
                .capacidadLibras(vehiculoRequest.getCapacidadLibras())
                .tipo(vehiculoRequest.getTipo())
                .estado(true)
                .situacion(SituacionVehiculo.DISPONIBLE)
                .build();

        VehiculoEntity guardado = vehiculoRepository.save(entity);
        return ApiResponse.success(guardado.getIdVehiculo(), "Vehículo registrado correctamente");
    }

    @Override
    public ApiResponse<Integer> actualizarVehiculo(int idVehiculo, VehiculoRequest vehiculoRequest) {
        Optional<VehiculoEntity> entityOptional = vehiculoRepository.findById(idVehiculo);

        if (entityOptional.isEmpty()) {
            return ApiResponse.error("Vehículo no encontrado");
        }

        VehiculoEntity entity = entityOptional.get();
        entity.setPlaca(vehiculoRequest.getPlaca());
        entity.setMarca(vehiculoRequest.getMarca());
        entity.setModelo(vehiculoRequest.getModelo());
        entity.setCapacidadLibras(vehiculoRequest.getCapacidadLibras());
        entity.setTipo(vehiculoRequest.getTipo());
        // La situación no se toca ni se altera en la edición

        VehiculoEntity actualizado = vehiculoRepository.save(entity);
        return ApiResponse.success(actualizado.getIdVehiculo(), "Vehículo actualizado correctamente");
    }

    @Override
    public ApiResponse<VehiculoResponse> obtenerVehiculoById(int idVehiculo) {
        Optional<VehiculoEntity> entityOptional = vehiculoRepository.findById(idVehiculo);

        if (entityOptional.isEmpty()) {
            return ApiResponse.error("Vehículo no encontrado");
        }

        VehiculoEntity entity = entityOptional.get();
        return ApiResponse.success(mapearA_Response(entity), "Vehículo encontrado correctamente");
    }

    @Override
    public ApiResponse<String> cambiarEstadoVehiculo(int idVehiculo, boolean nuevoEstado) {
        return vehiculoRepository.findById(idVehiculo)
                .map(entity -> {
                    entity.setEstado(nuevoEstado);
                    vehiculoRepository.save(entity);
                    String mensaje = nuevoEstado ? "Vehículo activado correctamente" : "Vehículo desactivado correctamente";
                    return ApiResponse.success(mensaje, mensaje);
                })
                .orElseGet(() -> ApiResponse.error("Vehículo no encontrado"));
    }

    private VehiculoResponse mapearA_Response(VehiculoEntity entity) {
        return VehiculoResponse.builder()
                .idVehiculo(entity.getIdVehiculo())
                .fechaRegistro(entity.getFechaRegistro())
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .capacidadLibras(entity.getCapacidadLibras())
                .tipo(entity.getTipo())
                .estado(entity.isEstado())
                .situacion(entity.getSituacion())
                .build();
    }
}
