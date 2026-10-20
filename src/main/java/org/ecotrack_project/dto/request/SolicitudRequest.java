package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudRequest {

    @NotBlank(message = "El titulo no debe estar vacio")
    private String titulo;

    @NotBlank(message = "La descripcion no debe estar vacia")
    private String descripcion;

    @NotBlank(message = "La ubicacion no debe estar vacia")
    private String ubicacion;
}