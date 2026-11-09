package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.ecotrack_project.dto.request.CargaRequest;
import org.ecotrack_project.dto.request.FinalizarCargaRequest;
import org.ecotrack_project.dto.request.IniciarCargaRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.CantidadCargasResponse;
import org.ecotrack_project.dto.response.CargaProcesoResponse;
import org.ecotrack_project.dto.response.CargaResponse;
import org.ecotrack_project.service.CargaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/carga")
@SecurityRequirement(name = "bearerAuth")
public class CargaController {

    private final CargaService cargaService;

    public CargaController(CargaService cargaService) {
        this.cargaService = cargaService;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<ApiResponse<CargaProcesoResponse>> iniciarCarga(
            @Valid @RequestBody IniciarCargaRequest request
    ) {
        ApiResponse<CargaProcesoResponse> response = cargaService.iniciarCarga(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/salida/{idCola}")
    public ResponseEntity<ApiResponse<CargaProcesoResponse>> consultarCargaPorCola(
            @PathVariable int idCola
    ) {
        ApiResponse<CargaProcesoResponse> response =
                cargaService.consultarCargaPorCola(idCola);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/finalizar")
    public ResponseEntity<ApiResponse<Integer>> finalizarCarga(
            @Valid @RequestBody FinalizarCargaRequest request
    ) {
        ApiResponse<Integer> response = cargaService.finalizarCarga(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<Integer>> registrarCarga(
            @Valid @RequestBody CargaRequest request
    ) {
        ApiResponse<Integer> response = cargaService.registrarCarga(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/programacion/{idProgramacion}/listar")
    public ResponseEntity<ApiResponse<List<CargaResponse>>> listarCargasPorProgramacion(
            @PathVariable int idProgramacion
    ) {
        ApiResponse<List<CargaResponse>> response =
                cargaService.listarCargasPorProgramacion(idProgramacion);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/rendimiento/paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginasRendimiento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        var response = cargaService.obtenerTotalPaginasRendimiento(fechaInicio, fechaFin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rendimiento/listar/{pagina}")
    public ResponseEntity<ApiResponse<List<CantidadCargasResponse>>> obtenerRendimientoCargasPaginado(
            @PathVariable int pagina,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        var response = cargaService.obtenerRendimientoCargasPaginado(fechaInicio, fechaFin, pagina);
        return ResponseEntity.ok(response);
    }
}