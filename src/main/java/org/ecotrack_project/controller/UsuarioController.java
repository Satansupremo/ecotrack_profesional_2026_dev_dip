
package org.ecotrack_project.controller;

import jakarta.validation.Valid;
import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        var response = usuarioService.obtenerToken(loginRequest);

        if (response.isSuccess())
            return ResponseEntity.status(HttpStatus.OK).body(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
