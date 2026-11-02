package org.ecotrack_project.controller;

import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ReservaUnidadResponse;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.service.ReservaUnidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reserva-unidad")
public class ReservaUnidadController {

    private final ReservaUnidadService reservaUnidadService;

    public ReservaUnidadController(ReservaUnidadService reservaUnidadService) {
        this.reservaUnidadService = reservaUnidadService;
    }

    @PostMapping("/{idProgramacion}/reservar")
    public ResponseEntity<ApiResponse<ReservaUnidadResponse>> reservarUnidadEmergencia(
            @PathVariable int idProgramacion,
            @RequestParam TipoVehiculo tipo
    ) {
        ApiResponse<ReservaUnidadResponse> response =
                reservaUnidadService.reservarUnidadEmergencia(idProgramacion, tipo);

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @GetMapping("/{idProgramacion}")
    public ResponseEntity<ApiResponse<List<ReservaUnidadResponse>>> listarReservasEmergencia(
            @PathVariable int idProgramacion
    ) {
        ApiResponse<List<ReservaUnidadResponse>> response =
                reservaUnidadService.listarReservasEmergencia(idProgramacion);

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @DeleteMapping("/{idProgramacion}/{idReserva}")
    public ResponseEntity<ApiResponse<Integer>> liberarReservaEmergencia(
            @PathVariable int idProgramacion,
            @PathVariable int idReserva
    ) {
        ApiResponse<Integer> response =
                reservaUnidadService.liberarReservaEmergencia(idProgramacion, idReserva);

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @PostMapping("/{idProgramacion}/confirmar")
    public ResponseEntity<ApiResponse<Integer>> confirmarUnidadesEmergencia(
            @PathVariable int idProgramacion
    ) {
        ApiResponse<Integer> response =
                reservaUnidadService.confirmarUnidadesEmergencia(idProgramacion);

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }

    @DeleteMapping("/{idProgramacion}/cancelar")
    public ResponseEntity<ApiResponse<Integer>> cancelarReservasEmergencia(
            @PathVariable int idProgramacion
    ) {
        ApiResponse<Integer> response =
                reservaUnidadService.cancelarReservasEmergencia(idProgramacion);

        return ResponseEntity.status(
                response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }
}