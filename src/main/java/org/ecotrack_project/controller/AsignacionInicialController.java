package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.AsignacionInicialRequest;
import org.ecotrack_project.dto.request.PagoInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.AsignacionInicialResponse;
import org.ecotrack_project.service.AsignacionInicialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asignacion-inicial")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AsignacionInicialController {

    private final AsignacionInicialService asignacionInicialService;

    @PostMapping("/registrar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> registrarAsignacionInicial(@PathVariable int idSolicitud, @Valid @RequestBody AsignacionInicialRequest request) {

        var response = asignacionInicialService.registrarAsignacionInicial(idSolicitud, request);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/buscar/{idSolicitud}")
    public ResponseEntity<ApiResponse<AsignacionInicialResponse>> obtenerAsignacionInicial(
            @PathVariable int idSolicitud) {

        var response = asignacionInicialService.obtenerAsignacionInicial(idSolicitud);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/validar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Boolean>> validarAsignacionInicial(
            @PathVariable int idSolicitud,
            @Valid @RequestBody AsignacionInicialRequest request) {

        var response = asignacionInicialService.validarAsignacionInicial(idSolicitud);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }
}