package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.SolicitudRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SolicitudResponse;
import org.ecotrack_project.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitud")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @GetMapping("/paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginas() {

        var response = solicitudService.obtenerTotalPaginas();

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalSolicitudesByIdUsuario() {

        var response = solicitudService.obtenerTotalSolicitudesByIdUsuario();

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/listar")
    public ResponseEntity<ApiResponse<List<SolicitudResponse>>> listarSolicitudes(
            @RequestParam(defaultValue = "0") int pagina) {

        var response = solicitudService.listarSolicitudes(pagina);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<Integer>> registrarSolicitud(
            @Valid @RequestBody SolicitudRequest solicitudRequest) {

        var response = solicitudService.registrarSolicitud(solicitudRequest);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/buscar/{idSolicitud}")
    public ResponseEntity<ApiResponse<SolicitudResponse>> obtenerSolicitud(
            @PathVariable int idSolicitud) {

        var response = solicitudService.obtenerSolicitud(idSolicitud);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/pendientes/paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginasPendientes() {

        var response = solicitudService.obtenerTotalPaginasPendientes();

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/pendientes/total")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalSolicitudesPendientes() {

        var response = solicitudService.obtenerTotalSolicitudesPendientes();

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/pendientes/listar")
    public ResponseEntity<ApiResponse<List<SolicitudResponse>>> listarSolicitudesPendientes(
            @RequestParam(defaultValue = "0") int pagina) {

        var response = solicitudService.listarSolicitudesPendientes(pagina);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/pendientes/buscar/{idSolicitud}")
    public ResponseEntity<ApiResponse<SolicitudResponse>> obtenerSolicitudPendiente(
            @PathVariable int idSolicitud) {

        var response = solicitudService.obtenerSolicitudPendiente(idSolicitud);

        if (response.isSuccess())
            return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }
}