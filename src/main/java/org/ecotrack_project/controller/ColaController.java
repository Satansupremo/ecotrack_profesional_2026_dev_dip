package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ColaResponse;
import org.ecotrack_project.service.ColaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cola")
@SecurityRequirement(name = "bearerAuth")
public class ColaController {

    private final ColaService colaService;

    public ColaController(ColaService colaService) {
        this.colaService = colaService;
    }

    @PutMapping("/{idCola}/recepcionar")
    public ResponseEntity<ApiResponse<Integer>> recepcionarSalida(
            @PathVariable int idCola
    ) {

        ApiResponse<Integer> response =
                colaService.recepcionarSalida(idCola);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/programacion/{idProgramacion}/listar")
    public ResponseEntity<ApiResponse<List<ColaResponse>>> listarSalidasPorProgramacion(
            @PathVariable int idProgramacion
    ) {

        ApiResponse<List<ColaResponse>> response =
                colaService.listarSalidasPorProgramacion(idProgramacion);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}