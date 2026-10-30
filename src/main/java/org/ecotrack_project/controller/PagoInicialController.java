package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.PagoInicialRequest;
import org.ecotrack_project.dto.request.RechazoPagoInicialRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.PagoInicialDetalleResponse;
import org.ecotrack_project.service.PagoInicialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pago-inicial")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PagoInicialController {

    private final PagoInicialService pagoInicialService;

    @PostMapping("/pago/registrar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> registrarPagoInicial(@PathVariable int idSolicitud, @Valid @RequestBody PagoInicialRequest request) {

        var response = pagoInicialService.registrarPagoInicial(idSolicitud, request);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/pie/detalle/{idSolicitud}")
    public ResponseEntity<ApiResponse<PagoInicialDetalleResponse>> obtenerDetallePagoInicial(
            @PathVariable int idSolicitud) {

        var response = pagoInicialService.obtenerDetallePagoInicial(idSolicitud);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/pie/confirmar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> confirmarPagoInicial(
            @PathVariable int idSolicitud) {

        var response = pagoInicialService.confirmarPagoInicial(idSolicitud);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/pie/rechazar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> rechazarPagoInicial(
            @PathVariable int idSolicitud,
            @Valid @RequestBody RechazoPagoInicialRequest request) {

        var response = pagoInicialService.rechazarPagoInicial(
                idSolicitud,
                request
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

}
