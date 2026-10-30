
package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.NotificacionResponse;
import org.ecotrack_project.entity.NotificacionEntity;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.NotificacionRepository;
import org.ecotrack_project.service.NotificacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

    private String obtenerUsernameAutenticado() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }

    @Override
    public ApiResponse<Integer> obtenerTotalNotificaciones() {

        String username = obtenerUsernameAutenticado();

        int total = (int) notificacionRepository.countByUsuario_Username(username);

        return ApiResponse.success(
                total,
                "Total de notificaciones obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalPaginas() {

        String username = obtenerUsernameAutenticado();

        long total = notificacionRepository.countByUsuario_Username(username);

        int paginas = (int) Math.ceil(
                (double) total / Initialize.TAMANIO_PAGINA
        );

        return ApiResponse.success(
                paginas,
                "Total de páginas obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<Long> contarMisNotificacionesNoLeidas() {

        String username = obtenerUsernameAutenticado();

        long cantidad = notificacionRepository
                .countByUsuario_UsernameAndEstadoFalse(username);

        return ApiResponse.success(
                cantidad,
                "Total de notificaciones no leídas obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<List<NotificacionResponse>> listarMisNotificaciones(int pagina) {

        String username = obtenerUsernameAutenticado();

        Pageable pageable = PageRequest.of(
                pagina,
                Initialize.TAMANIO_PAGINA
        );

        Page<NotificacionEntity> resultado =
                notificacionRepository
                        .findByUsuario_UsernameOrderByIdNotificacionAsc(
                                username,
                                pageable
                        );

        List<NotificacionResponse> notificaciones = resultado.getContent()
                .stream()
                .map(notificacion -> NotificacionResponse.builder()
                        .idNotificacion(notificacion.getIdNotificacion())
                        .descripcion(notificacion.getDescripcion())
                        .estado(notificacion.isEstado())
                        .build())
                .toList();

        return ApiResponse.success(
                notificaciones,
                "Notificaciones obtenidas correctamente"
        );
    }

    @Override
    @Transactional
    public ApiResponse<Integer> marcarComoLeida(int idNotificacion) {

        String username = obtenerUsernameAutenticado();

        Optional<NotificacionEntity> notificacionOptional =
                notificacionRepository.findByIdNotificacionAndUsuario_Username(
                        idNotificacion,
                        username
                );

        if (notificacionOptional.isEmpty()) {
            return ApiResponse.error("Notificación no encontrada");
        }

        NotificacionEntity notificacion = notificacionOptional.get();

        if (notificacion.isEstado()) {
            return ApiResponse.success(
                    notificacion.getIdNotificacion(),
                    "La notificación ya fue leída"
            );
        }

        notificacion.setEstado(true);
        notificacionRepository.save(notificacion);

        return ApiResponse.success(
                notificacion.getIdNotificacion(),
                "Notificación marcada como leída correctamente"
        );
    }
}
