
package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 150, message = "El correo no debe superar los 150 caracteres")
    private String username;

    @NotBlank(message = "La password es obligatoria")
    @Size(min = 4, max = 100, message = "La password debe tener entre 4 y 100 caracteres")
    private String password;
}
