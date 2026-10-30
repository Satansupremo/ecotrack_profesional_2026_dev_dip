
package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.NotificacionResponse;
import org.ecotrack_project.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacion")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalNotificaciones() {

        ApiResponse<Integer> response =
                notificacionService.obtenerTotalNotificaciones();

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }

    @GetMapping("/paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginas() {

        ApiResponse<Integer> response =
                notificacionService.obtenerTotalPaginas();

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }

    @GetMapping("/mis-notificaciones")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listarMisNotificaciones(
            @RequestParam(defaultValue = "0") int pagina) {

        ApiResponse<List<NotificacionResponse>> response =
                notificacionService.listarMisNotificaciones(pagina);

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }

    @GetMapping("/no-leidas/total")
    public ResponseEntity<ApiResponse<Long>> contarMisNotificacionesNoLeidas() {

        ApiResponse<Long> response =
                notificacionService.contarMisNotificacionesNoLeidas();

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }

    @PutMapping("/{idNotificacion}/leer")
    public ResponseEntity<ApiResponse<Integer>> marcarComoLeida(
            @PathVariable int idNotificacion) {

        ApiResponse<Integer> response =
                notificacionService.marcarComoLeida(idNotificacion);

        return ResponseEntity
                .status(response.isSuccess() ? 200 : 400)
                .body(response);
    }
}
