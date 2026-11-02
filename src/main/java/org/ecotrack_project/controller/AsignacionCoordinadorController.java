
package org.ecotrack_project.controller;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.service.AsignacionCoordinadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asignacion-coordinador")
@RequiredArgsConstructor
public class AsignacionCoordinadorController {

    private final AsignacionCoordinadorService asignacionCoordinadorService;

    @PostMapping("/tomar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Integer>> tomarSolicitud(
            @PathVariable int idSolicitud) {

        ApiResponse<Integer> response =
                asignacionCoordinadorService.tomarSolicitud(idSolicitud);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/cancelar/{idSolicitud}")
    public ResponseEntity<ApiResponse<Void>> cancelarSeleccion(
            @PathVariable int idSolicitud) {

        ApiResponse<Void> response =
                asignacionCoordinadorService.cancelarSeleccion(idSolicitud);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
