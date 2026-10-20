package org.ecotrack_project.controller;

import jakarta.validation.Valid;
import org.ecotrack_project.dto.request.VehiculoRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.VehiculoResponse;
import org.ecotrack_project.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculo")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @GetMapping("/paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginas() {
        var response = vehiculoService.obtenerTotalPaginas();

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<VehiculoResponse>>> listarVehiculo(
            @RequestParam(defaultValue = "0") int pagina) {

        var response = vehiculoService.listarVehiculo(pagina);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/buscar/{idVehiculo}")
    public ResponseEntity<ApiResponse<VehiculoResponse>> obtenerVehiculoById(
            @PathVariable int idVehiculo) {

        var response = vehiculoService.obtenerVehiculoById(idVehiculo);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<Integer>> registrarVehiculo(
            @Valid @RequestBody VehiculoRequest vehiculoRequest) {

        var response = vehiculoService.registrarVehiculo(vehiculoRequest);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/actualizar/{idVehiculo}")
    public ResponseEntity<ApiResponse<Integer>> actualizarVehiculo(
            @PathVariable int idVehiculo,
            @Valid @RequestBody VehiculoRequest vehiculoRequest) {

        var response = vehiculoService.actualizarVehiculo(idVehiculo, vehiculoRequest);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}