
package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.SolicitudRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SolicitudResponse;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.SolicitudRepository;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.service.SolicitudService;
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
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    private Optional<UsuarioEntity> obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

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

        int totalPaginas = (int) Math.ceil(
                (double) totalRegistros / Initialize.TAMANIO_PAGINA
        );

        return ApiResponse.success(
                totalPaginas,
                "Total de páginas obtenidas correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalSolicitudesByIdUsuario() {
        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario no encontrado");
        }

        int idUsuario = usuarioOptional.get().getIdUsuario();

        int totalSolicitudes = (int) solicitudRepository
                .countByUsuario_IdUsuario(idUsuario);

        return ApiResponse.success(
                totalSolicitudes,
                "Total de solicitudes obtenidas correctamente"
        );
    }

    @Override
    public ApiResponse<List<SolicitudResponse>> listarSolicitudes(int pagina) {
        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario no encontrado");
        }

        if (pagina < 0) {
            return ApiResponse.error("El número de página no es válido");
        }

        int idUsuario = usuarioOptional.get().getIdUsuario();

        Pageable pageable = PageRequest.of(
                pagina,
                Initialize.TAMANIO_PAGINA
        );

        Page<SolicitudEntity> solicitudesPage = solicitudRepository
                .findByUsuario_IdUsuario(idUsuario, pageable);

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
                .fechaFinalizacion(null)
                .build();

        SolicitudEntity guardado = solicitudRepository.save(entity);

        return ApiResponse.success(
                guardado.getIdSolicitud(),
                "Solicitud registrada correctamente"
        );
    }

    @Override
    public ApiResponse<SolicitudResponse> obtenerSolicitud(int idSolicitud) {
        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty()) {
            return ApiResponse.error("Usuario no encontrado");
        }

        int idUsuario = usuarioOptional.get().getIdUsuario();

        Optional<SolicitudEntity> entityOptional = solicitudRepository
                .findByIdSolicitudAndUsuario_IdUsuario(idSolicitud, idUsuario);

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

        return ApiResponse.success(
                response,
                "Solicitud obtenida correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalSolicitudesPendientes() {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty() ||
                (usuarioOptional.get().getRol() != Rol.COORDINADOR &&
                        usuarioOptional.get().getRol() != Rol.ADMINISTRADOR)) {
            return ApiResponse.error("Usuario no encontrado o no autorizado");
        }

        int totalSolicitudes = (int) solicitudRepository
                .countByEstado(EstadoSolicitud.PENDIENTE);

        return ApiResponse.success(
                totalSolicitudes,
                "Total de solicitudes pendientes obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalPaginasPendientes() {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty() ||
                (usuarioOptional.get().getRol() != Rol.COORDINADOR &&
                        usuarioOptional.get().getRol() != Rol.ADMINISTRADOR)) {
            return ApiResponse.error("Usuario no encontrado o no autorizado");
        }

        long totalRegistros = solicitudRepository
                .countByEstado(EstadoSolicitud.PENDIENTE);

        int totalPaginas = (int) Math.ceil(
                (double) totalRegistros / Initialize.TAMANIO_PAGINA
        );

        return ApiResponse.success(
                totalPaginas,
                "Total de páginas de solicitudes pendientes obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<List<SolicitudResponse>> listarSolicitudesPendientes(int pagina) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty() ||
                (usuarioOptional.get().getRol() != Rol.COORDINADOR &&
                        usuarioOptional.get().getRol() != Rol.ADMINISTRADOR)) {
            return ApiResponse.error("Usuario no encontrado o no autorizado");
        }

        if (pagina < 0) {
            return ApiResponse.error("El número de página no es válido");
        }

        Pageable pageable = PageRequest.of(
                pagina,
                Initialize.TAMANIO_PAGINA
        );

        Page<SolicitudEntity> solicitudesPage = solicitudRepository
                .findByEstado(EstadoSolicitud.PENDIENTE, pageable);

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
                "Lista de solicitudes pendientes obtenida correctamente"
        );
    }

    @Override
    public ApiResponse<SolicitudResponse> obtenerSolicitudPendiente(int idSolicitud) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty() ||
                (usuarioOptional.get().getRol() != Rol.COORDINADOR &&
                        usuarioOptional.get().getRol() != Rol.ADMINISTRADOR)) {
            return ApiResponse.error("Usuario no encontrado o no autorizado");
        }

        Optional<SolicitudEntity> entityOptional = solicitudRepository
                .findByIdSolicitudAndEstado(
                        idSolicitud,
                        EstadoSolicitud.PENDIENTE
                );

        if (entityOptional.isEmpty()) {
            return ApiResponse.error("Solicitud pendiente no encontrada");
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

        return ApiResponse.success(
                response,
                "Solicitud pendiente obtenida correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalSolicitudesPIE() {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty() ||
                (usuarioOptional.get().getRol() != Rol.COORDINADOR &&
                        usuarioOptional.get().getRol() != Rol.ADMINISTRADOR)) {
            return ApiResponse.error("Usuario no encontrado o no autorizado");
        }

        int totalSolicitudes = (int) solicitudRepository
                .countByEstado(EstadoSolicitud.PAGO_INICIAL_EFECTUADO);

        return ApiResponse.success(
                totalSolicitudes,
                "Total de solicitudes PIE obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalPaginasPIE() {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty() ||
                (usuarioOptional.get().getRol() != Rol.COORDINADOR &&
                        usuarioOptional.get().getRol() != Rol.ADMINISTRADOR)) {
            return ApiResponse.error("Usuario no encontrado o no autorizado");
        }

        long totalRegistros = solicitudRepository
                .countByEstado(EstadoSolicitud.PAGO_INICIAL_EFECTUADO);

        int totalPaginas = (int) Math.ceil(
                (double) totalRegistros / Initialize.TAMANIO_PAGINA
        );

        return ApiResponse.success(
                totalPaginas,
                "Total de páginas de solicitudes PIE obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<List<SolicitudResponse>> listarSolicitudesPIE(int pagina) {

        Optional<UsuarioEntity> usuarioOptional = obtenerUsuarioAutenticado();

        if (usuarioOptional.isEmpty() ||
                (usuarioOptional.get().getRol() != Rol.COORDINADOR &&
                        usuarioOptional.get().getRol() != Rol.ADMINISTRADOR)) {
            return ApiResponse.error("Usuario no encontrado o no autorizado");
        }

        if (pagina < 0) {
            return ApiResponse.error("El número de página no es válido");
        }

        Pageable pageable = PageRequest.of(
                pagina,
                Initialize.TAMANIO_PAGINA,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.ASC,
                        "fechaRegistro"
                ).and(
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.ASC,
                                "idSolicitud"
                        )
                )
        );

        Page<SolicitudEntity> solicitudesPage = solicitudRepository
                .findByEstado(EstadoSolicitud.PAGO_INICIAL_EFECTUADO, pageable);

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
                "Lista de solicitudes PIE obtenida correctamente"
        );
    }

}
