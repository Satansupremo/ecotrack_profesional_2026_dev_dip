package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.request.UsuarioRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}