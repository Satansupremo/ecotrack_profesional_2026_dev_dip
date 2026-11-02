
package org.ecotrack_project.controller;

import jakarta.validation.Valid;
import org.ecotrack_project.dto.request.ProgramacionRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.ProgramacionResponse;
import org.ecotrack_project.dto.response.UnidadProgramacionResponse;
import org.ecotrack_project.enums.TipoVehiculo;
import org.ecotrack_project.service.ProgramacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/programacion")
public class ProgramacionController {

    private final ProgramacionService programacionService;

    public ProgramacionController(ProgramacionService programacionService) {
        this.programacionService = programacionService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<Integer>> registrarProgramacion(
            @Valid @RequestBody ProgramacionRequest request
    ) {
        ApiResponse<Integer> response =
                programacionService.registrarProgramacion(request);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/fecha/total")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalProgramacionesPorFecha(
            @RequestParam LocalDate fecha
    ) {
        ApiResponse<Integer> response =
                programacionService.obtenerTotalProgramacionesPorFecha(fecha);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/fecha/paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginasProgramacionesPorFecha(
            @RequestParam LocalDate fecha
    ) {
        ApiResponse<Integer> response =
                programacionService.obtenerTotalPaginasProgramacionesPorFecha(fecha);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/fecha/listar")
    public ResponseEntity<ApiResponse<List<ProgramacionResponse>>> listarProgramacionesPorFecha(
            @RequestParam LocalDate fecha,
            @RequestParam(defaultValue = "0") int pagina
    ) {
        ApiResponse<List<ProgramacionResponse>> response =
                programacionService.listarProgramacionesPorFecha(fecha, pagina);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/detalle/{idProgramacion}")
    public ResponseEntity<ApiResponse<ProgramacionResponse>> obtenerDetalleProgramacion(
            @PathVariable int idProgramacion
    ) {
        ApiResponse<ProgramacionResponse> response =
                programacionService.obtenerDetalleProgramacion(idProgramacion);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/mis-programaciones/total")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalMisProgramaciones() {

        ApiResponse<Integer> response =
                programacionService.obtenerTotalMisProgramaciones();

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/mis-programaciones/paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginasMisProgramaciones() {

        ApiResponse<Integer> response =
                programacionService.obtenerTotalPaginasMisProgramaciones();

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/mis-programaciones/listar")
    public ResponseEntity<ApiResponse<List<ProgramacionResponse>>> listarMisProgramaciones(
            @RequestParam(defaultValue = "0") int pagina
    ) {
        ApiResponse<List<ProgramacionResponse>> response =
                programacionService.listarMisProgramaciones(pagina);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/mis-programaciones/{idProgramacion}/unidades")
    public ResponseEntity<ApiResponse<List<UnidadProgramacionResponse>>> listarUnidadesProgramacion(
            @PathVariable int idProgramacion
    ) {

        ApiResponse<List<UnidadProgramacionResponse>> response =
                programacionService.listarUnidadesProgramacion(idProgramacion);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/mis-programaciones/{idProgramacion}/unidades")
    public ResponseEntity<ApiResponse<UnidadProgramacionResponse>> agregarUnidadProgramacion(
            @PathVariable int idProgramacion,
            @RequestParam TipoVehiculo tipo
    ) {

        ApiResponse<UnidadProgramacionResponse> response =
                programacionService.agregarUnidadProgramacion(idProgramacion, tipo);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/mis-programaciones/{idProgramacion}/unidades/{idDetalle}")
    public ResponseEntity<ApiResponse<Integer>> eliminarUnidadProgramacion(
            @PathVariable int idProgramacion,
            @PathVariable int idDetalle
    ) {

        ApiResponse<Integer> response =
                programacionService.eliminarUnidadProgramacion(idProgramacion, idDetalle);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/mis-programaciones/{idProgramacion}/ejecutar")
    public ResponseEntity<ApiResponse<Integer>> ejecutarProgramacion(
            @PathVariable int idProgramacion
    ) {

        ApiResponse<Integer> response =
                programacionService.ejecutarProgramacion(idProgramacion);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/mis-programaciones/{idProgramacion}/detalle")
    public ResponseEntity<ApiResponse<ProgramacionResponse>> obtenerDetalleMiProgramacion(
            @PathVariable int idProgramacion
    ) {

        ApiResponse<ProgramacionResponse> response =
                programacionService.obtenerDetalleMiProgramacion(idProgramacion);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

}
