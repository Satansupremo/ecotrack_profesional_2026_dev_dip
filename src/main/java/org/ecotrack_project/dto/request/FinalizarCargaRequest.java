package org.ecotrack_project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarCargaRequest {

    @NotNull(message = "El identificador de la carga es obligatorio.")
    private Integer idCarga;

    @NotEmpty(message = "Debe registrar al menos un artículo recuperado.")
    @Valid
    private List<ArticuloRecuperadoRequest> articulos;
}