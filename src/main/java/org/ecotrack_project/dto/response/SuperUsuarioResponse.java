package org.ecotrack_project.dto.response;

import lombok.*;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperUsuarioResponse {

    private int idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String celular;
    private String username;
    private String dni;
    private boolean estado;
    private Rol rol;
    private SituacionUsuario situacion; // NINGUNO, DISPONIBLE, ASIGNADO, RESERVADO, INACTIVO
}
