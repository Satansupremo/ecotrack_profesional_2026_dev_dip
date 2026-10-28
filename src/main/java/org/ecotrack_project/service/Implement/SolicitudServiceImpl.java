
package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.SolicitudRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SolicitudResponse;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.SolicitudRepository;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.service.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Optional<UsuarioEntity> obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return usuarioRepository.findByUsername(username);
    }

    @Override
    public ApiResponse<Integer> obtenerTotalPaginas() {
        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario no encontrado");
        }

        int idUsuario = usuarioOptional.get().getIdUsuario();

        long totalRegistros = solicitudRepository.countByUsuario_IdUsuario(idUsuario);

        int totalPaginas = (int) Math.ceil((double) totalRegistros / Initialize.TAMANIO_PAGINA);

        return ApiResponse.success(totalPaginas, "Total de páginas obtenidas correctamente");
    }

    @Override
    public ApiResponse<Integer> obtenerTotalSolicitudesByIdUsuario() {
        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario no encontrado");
        }

        int idUsuario = usuarioOptional.get().getIdUsuario();

        int totalSolicitudes = (int) solicitudRepository.countByUsuario_IdUsuario(idUsuario);

        return ApiResponse.success(totalSolicitudes, "Total de solicitudes obtenidas correctamente");
    }

    @Override
    public ApiResponse<List<SolicitudResponse>> listarSolicitudes(int pagina) {
        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario no encontrado");
        }

        int idUsuario = usuarioOptional.get().getIdUsuario();

        Pageable pageable = PageRequest.of(pagina, Initialize.TAMANIO_PAGINA);

        Page<SolicitudEntity> solicitudesPage = solicitudRepository.findByUsuario_IdUsuario(idUsuario, pageable);

        List<SolicitudResponse> response = solicitudesPage.getContent()
                .stream()
                .map(entity -> SolicitudResponse.builder()
                        .idSolicitud(entity.getIdSolicitud())
                        .titulo(entity.getTitulo())
                        .descripcion(entity.getDescripcion())
                        .ubicacion(entity.getUbicacion())
                        .fechaRegistro(entity.getFechaRegistro())
                        .fechaFinalizacion(entity.getFechaFinalizacion())
                        .estado(entity.getEstado().name())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(
                response,
                "Lista de solicitudes obtenida correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> registrarSolicitud(SolicitudRequest solicitudRequest) {
        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario no encontrado");
        }

        SolicitudEntity entity = SolicitudEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .titulo(solicitudRequest.getTitulo())
                .descripcion(solicitudRequest.getDescripcion())
                .ubicacion(solicitudRequest.getUbicacion())
                .estado(EstadoSolicitud.PENDIENTE)
                .usuario(usuarioOptional.get())
                .coordinador(null)
                .fechaFinalizacion(null)
                .build();

        SolicitudEntity guardado = solicitudRepository.save(entity);

        return ApiResponse.success(guardado.getIdSolicitud(), "Solicitud registrada correctamente");
    }

    @Override
    public ApiResponse<SolicitudResponse> obtenerSolicitud(int idSolicitud) {
        Optional<SolicitudEntity> entityOptional = solicitudRepository.findById(idSolicitud);

        if (entityOptional.isEmpty()) {
            return ApiResponse.error("Solicitud no encontrada");
        }

        SolicitudEntity entity = entityOptional.get();

        SolicitudResponse response = SolicitudResponse.builder()
                .idSolicitud(entity.getIdSolicitud())
                .titulo(entity.getTitulo())
                .descripcion(entity.getDescripcion())
                .ubicacion(entity.getUbicacion())
                .fechaRegistro(entity.getFechaRegistro())
                .fechaFinalizacion(entity.getFechaFinalizacion())
                .estado(entity.getEstado().name())
                .build();

        return ApiResponse.success(response, "Solicitud obtenida correctamente");
    }
}
