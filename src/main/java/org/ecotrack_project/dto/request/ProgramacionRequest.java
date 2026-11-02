package org.ecotrack_project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramacionRequest {

    @NotNull(message = "El código de solicitud es obligatorio")
    private Integer idSolicitud;

    @NotNull(message = "La fecha de ejecución es obligatoria")
    private LocalDate fechaEjecucion;
}