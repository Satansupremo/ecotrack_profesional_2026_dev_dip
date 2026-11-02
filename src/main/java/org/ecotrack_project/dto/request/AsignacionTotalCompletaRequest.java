package org.ecotrack_project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AsignacionTotalCompletaRequest {

    @NotNull(message = "La asignación total es obligatoria")
    @Valid
    private AsignacionTotalRequest asignacionTotal;

    @NotNull(message = "La recomendación es obligatoria")
    @Valid
    private RecomendacionRequest recomendacion;
}