
package org.ecotrack_project.service;

import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.NotificacionResponse;

import java.util.List;

public interface NotificacionService {

    ApiResponse<Integer> obtenerTotalNotificaciones();
    ApiResponse<Integer> obtenerTotalPaginas();
    ApiResponse<Long> contarMisNotificacionesNoLeidas();
    ApiResponse<List<NotificacionResponse>> listarMisNotificaciones(int pagina);
    ApiResponse<Integer> marcarComoLeida(int idNotificacion);
}
