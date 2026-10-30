package org.ecotrack_project.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificacionResponse {

    private int idNotificacion;
    private String descripcion;
    private boolean estado;
}
