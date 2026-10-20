package org.ecotrack_project.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prueba")
@SecurityRequirement(name = "bearerAuth")
public class PruebaController {

    @GetMapping("/usuario")
    public String usuario() {
        return "Hola, soy un usuario";
    }

    @GetMapping("/administrador")
    public String administrador() {
        return "Hola, soy un administrador";
    }

    @GetMapping("/coordinador")
    public String coordinador() {
        return "Hola, soy un coordinador";
    }

    @GetMapping("/transportista")
    public String transportista() {
        return "Hola, soy un transportista";
    }

    @GetMapping("/operario")
    public String operario() {
        return "Hola, soy un operario";
    }
}