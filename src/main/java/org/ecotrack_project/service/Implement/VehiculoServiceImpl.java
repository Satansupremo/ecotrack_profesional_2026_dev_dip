package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.VehiculoRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.VehiculoResponse;
import org.ecotrack_project.entity.VehiculoEntity;
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

        return ApiResponse.success(totalPaginas, "Total de paginas obtenidas correctamente");
    }

    @Override
    public ApiResponse<List<VehiculoResponse>> listarVehiculo(int pagina) {
        Pageable pageable = PageRequest.of(pagina, Initialize.TAMANIO_PAGINA);
        Page<VehiculoEntity> vehiculosPage = vehiculoRepository.findAll(pageable);

        List<VehiculoResponse> response = vehiculosPage.getContent()
                .stream()
                .map(entity -> VehiculoResponse.builder()
                        .idVehiculo(entity.getIdVehiculo())
                        .fechaRegistro(entity.getFechaRegistro())
                        .placa(entity.getPlaca())
                        .marca(entity.getMarca())
                        .modelo(entity.getModelo())
                        .capacidadLibras(entity.getCapacidadLibras())
                        .estado(entity.isEstado())
                        .build())
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
                .estado(true)
                .build();

        VehiculoEntity guardado = vehiculoRepository.save(entity);
        return ApiResponse.success(guardado.getIdVehiculo(), "Vehiculo registrado correctamente");
    }

    @Override
    public ApiResponse<Integer> actualizarVehiculo(int idVehiculo, VehiculoRequest vehiculoRequest) {
        Optional<VehiculoEntity> entityOptional = vehiculoRepository.findById(idVehiculo);

        if (entityOptional.isEmpty()) {
            return ApiResponse.error("Vehiculo no encontrado");
        }

        VehiculoEntity entity = entityOptional.get();
        entity.setPlaca(vehiculoRequest.getPlaca());
        entity.setMarca(vehiculoRequest.getMarca());
        entity.setModelo(vehiculoRequest.getModelo());
        entity.setCapacidadLibras(vehiculoRequest.getCapacidadLibras());

        VehiculoEntity actualizado = vehiculoRepository.save(entity);
        return ApiResponse.success(actualizado.getIdVehiculo(), "Vehiculo actualizado correctamente");
    }

    @Override
    public ApiResponse<VehiculoResponse> obtenerVehiculoById(int idVehiculo) {
        Optional<VehiculoEntity> entityOptional = vehiculoRepository.findById(idVehiculo);

        if (entityOptional.isEmpty()) {
            return ApiResponse.error("Vehiculo no encontrado");
        }

        VehiculoEntity entity = entityOptional.get();
        VehiculoResponse response = VehiculoResponse.builder()
                .idVehiculo(entity.getIdVehiculo())
                .fechaRegistro(entity.getFechaRegistro())
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .capacidadLibras(entity.getCapacidadLibras())
                .estado(entity.isEstado())
                .build();

        return ApiResponse.success(response, "Vehiculo encontrado correctamente");
    }
}
