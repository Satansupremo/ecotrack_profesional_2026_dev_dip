package org.ecotrack_project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.PagoTotalRequest;
import org.ecotrack_project.dto.request.RechazoPagoTotalRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.PagoTotalDetalleResponse;
import org.ecotrack_project.service.PagoTotalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pago-total")
@RequiredArgsConstructor
public class PagoTotalController {

    private final PagoTotalService pagoTotalService;

    @PostMapping("/pago/registrar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> registrarPagoTotal(
            @PathVariable int idSolicitud,
            @Valid @RequestBody PagoTotalRequest request) {

        ApiResponse<Integer> response =
                pagoTotalService.registrarPagoTotal(idSolicitud, request);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @GetMapping("/ptt/detalle/{idSolicitud}")
    public ResponseEntity<ApiResponse<PagoTotalDetalleResponse>> obtenerDetallePagoTotal(
            @PathVariable int idSolicitud) {

        ApiResponse<PagoTotalDetalleResponse> response =
                pagoTotalService.obtenerDetallePagoTotal(idSolicitud);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PutMapping("/ptt/confirmar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> confirmarPagoTotal(
            @PathVariable int idSolicitud) {

        ApiResponse<Integer> response =
                pagoTotalService.confirmarPagoTotal(idSolicitud);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PutMapping("/ptt/rechazar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> rechazarPagoTotal(
            @PathVariable int idSolicitud,
            @Valid @RequestBody RechazoPagoTotalRequest request) {

        ApiResponse<Integer> response =
                pagoTotalService.rechazarPagoTotal(idSolicitud, request);

        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }
}