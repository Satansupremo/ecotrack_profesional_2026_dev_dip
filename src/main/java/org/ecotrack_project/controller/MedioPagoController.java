
package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.MedioPagoResponse;
import org.ecotrack_project.service.MedioPagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medio-pago")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class MedioPagoController {

    private final MedioPagoService medioPagoService;

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<MedioPagoResponse>>> listarMediosPago() {

        var response = medioPagoService.listarMediosPago();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
