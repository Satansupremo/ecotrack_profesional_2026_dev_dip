
package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.AsignacionTotalCompletaRequest;
import org.ecotrack_project.dto.request.AsignacionTotalRequest;
import org.ecotrack_project.dto.request.RecomendacionRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.AsignacionTotalResponse;
import org.ecotrack_project.service.AsignacionTotalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asignacion-total")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AsignacionTotalController {

    private final AsignacionTotalService asignacionTotalService;

    @PostMapping("/registrar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> registrarAsignacionTotal(
            @PathVariable int idSolicitud,
            @Valid @RequestBody AsignacionTotalCompletaRequest request) {

        var response = asignacionTotalService.registrarAsignacionTotal(
                idSolicitud,
                request.getAsignacionTotal(),
                request.getRecomendacion()
        );

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/buscar/{idSolicitud}")
    public ResponseEntity<ApiResponse<AsignacionTotalResponse>> obtenerAsignacionTotal(
            @PathVariable int idSolicitud) {

        var response = asignacionTotalService.obtenerAsignacionTotal(idSolicitud);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }


    @GetMapping("/verificar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Void>> verificarAsignacionTotal(
            @PathVariable int idSolicitud) {

        var response = asignacionTotalService.verificarAsignacionTotal(idSolicitud);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

}
