package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.request.SuperUsuarioRequest;
import org.ecotrack_project.dto.request.UsuarioRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SuperUsuarioResponse;
import org.ecotrack_project.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        var response = usuarioService.obtenerToken(loginRequest);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<Integer>> registrarCuenta(
            @Valid @RequestBody UsuarioRequest usuarioRequest) {

        var response = usuarioService.registrarCuenta(usuarioRequest);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/total-paginas")
    public ResponseEntity<ApiResponse<Integer>> obtenerTotalPaginas() {
        var response = usuarioService.obtenerTotalPaginas();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/listar/{pagina}")
    public ResponseEntity<ApiResponse<List<SuperUsuarioResponse>>> listarUsuarios(
            @PathVariable int pagina) {

        var response = usuarioService.listarUsuarios(pagina);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<SuperUsuarioResponse>> obtenerUsuarioPorId(@PathVariable int id) {

        var response = usuarioService.obtenerUsuarioPorId(id);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping("/crear-superusuario")
    public ResponseEntity<ApiResponse<Integer>> crearSuperUsuario(
            @Valid @RequestBody SuperUsuarioRequest request) {

        var response = usuarioService.crearSuperUsuario(request);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<ApiResponse<Integer>> actualizarSuperUsuario(
            @PathVariable int id,
            @Valid @RequestBody SuperUsuarioRequest request) {

        var response = usuarioService.actualizarSuperUsuario(id, request);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PatchMapping("/cambiar-estado/{id}")
    public ResponseEntity<ApiResponse<String>> cambiarEstadoUsuario(
            @PathVariable int id,
            @RequestParam boolean nuevoEstado) {

        var response = usuarioService.cambiarEstadoUsuario(id, nuevoEstado);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
