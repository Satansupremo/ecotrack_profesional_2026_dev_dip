package org.ecotrack_project.dto.response;

import lombok.*;
import org.ecotrack_project.enums.Rol;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CantidadCargasResponse {
    private int idEmpleado;
    private String nombreCompleto;
    private String dni;
    private Rol rol;
    private long totalCargas;
}
