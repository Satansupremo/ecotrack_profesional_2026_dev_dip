package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.request.SuperUsuarioRequest;
import org.ecotrack_project.dto.request.UsuarioRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SuperUsuarioResponse;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.security.JwtService;
import org.ecotrack_project.security.UsuarioDetails;
import org.ecotrack_project.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<String> obtenerToken(LoginRequest loginRequest) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginRequest.getUsername(),
                                    loginRequest.getPassword()
                            )
                    );

            UsuarioDetails usuarioDetails =
                    (UsuarioDetails) authentication.getPrincipal();

            String token = jwtService.generarToken(usuarioDetails);

            return ApiResponse.success(
                    token,
                    "¡¡ Iniciando sesión correctamente !!"
            );

        } catch (AuthenticationException exception) {
            return ApiResponse.error(
                    "Username o password incorrectos, o usuario inactivo"
            );
        }
    }

    @Override
    public ApiResponse<Integer> registrarCuenta(UsuarioRequest usuarioRequest) {
        if (usuarioRepository.existsByUsername(usuarioRequest.getUsername())) {
            return ApiResponse.error("El username ya está registrado - Digite otro");
        }
        if (usuarioRepository.existsByCorreo(usuarioRequest.getCorreo())) {
            return ApiResponse.error("El correo ya está registrado - Digite otro");
        }

        UsuarioEntity usuario = UsuarioEntity.builder()
                .nombre(usuarioRequest.getNombre())
                .apellido(usuarioRequest.getApellido())
                .dni(usuarioRequest.getDni())
                .correo(usuarioRequest.getCorreo())
                .celular(usuarioRequest.getCelular())
                .username(usuarioRequest.getUsername())
                .password(passwordEncoder.encode(usuarioRequest.getPassword()))
                .situacion(SituacionUsuario.NINGUNO)
                .estado(true)
                .rol(Rol.USUARIO)
                .build();

        usuarioRepository.save(usuario);

        return ApiResponse.success(
                usuario.getIdUsuario(),
                "El usuario se ha creado exitosamente"
        );
    }

    @Override
    public ApiResponse<UsuarioEntity> obtenerUsuarioLogueado() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            return ApiResponse.error(
                    "No hay un usuario autenticado"
            );
        }

        return ApiResponse.success(
                usuarioDetails.getUsuario(),
                "Usuario obtenido correctamente"
        );
    }

    @Override
    public ApiResponse<Integer> obtenerTotalPaginas() {
        long totalRegistros = usuarioRepository.countByRolNot(Rol.USUARIO);
        int totalPaginas = (int) Math.ceil((double) totalRegistros / Initialize.TAMANIO_PAGINA);

        return ApiResponse.success(totalPaginas, "Total de páginas de empleados obtenidas correctamente");
    }

    @Override
    public ApiResponse<List<SuperUsuarioResponse>> listarUsuarios(int pagina) {
        Pageable pageable = PageRequest.of(pagina, Initialize.TAMANIO_PAGINA);

        Page<UsuarioEntity> usuariosPage = usuarioRepository.findByRolNot(Rol.USUARIO, pageable);

        List<SuperUsuarioResponse> responseList = usuariosPage.getContent()
                .stream()
                .map(this::mapearA_Response)
                .collect(Collectors.toList());

        return ApiResponse.success(responseList, "Lista de empleados obtenida con éxito");
    }

    @Override
    public ApiResponse<SuperUsuarioResponse> obtenerUsuarioPorId(int id) {
        return usuarioRepository.findById(id)
                .map(usuario -> ApiResponse.success(mapearA_Response(usuario), "Usuario encontrado"))
                .orElseGet(() -> ApiResponse.error("Usuario no encontrado con el ID: " + id));
    }

    @Override
    public ApiResponse<Integer> crearSuperUsuario(SuperUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error("El username ya está registrado");
        }
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            return ApiResponse.error("El correo ya está registrado");
        }

        UsuarioEntity usuario = UsuarioEntity.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .correo(request.getCorreo())
                .celular(request.getCelular())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .dni(request.getDni())
                .rol(request.getRol())
                .situacion(SituacionUsuario.DISPONIBLE)
                .estado(true)
                .build();

        usuarioRepository.save(usuario);
        return ApiResponse.success(usuario.getIdUsuario(), "Superusuario creado correctamente");
    }

    @Override
    public ApiResponse<Integer> actualizarSuperUsuario(int id, SuperUsuarioRequest request) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(request.getNombre());
            usuario.setApellido(request.getApellido());
            usuario.setCorreo(request.getCorreo());
            usuario.setCelular(request.getCelular());
            usuario.setUsername(request.getUsername());
            usuario.setDni(request.getDni());

            if (usuario.getRol() != request.getRol()) {
                usuario.setRol(request.getRol());
                usuario.setSituacion(determinarSituacionPorRol(request.getRol()));
            }

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                usuario.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            usuarioRepository.save(usuario);
            return ApiResponse.success(usuario.getIdUsuario(), "Usuario actualizado correctamente");
        }).orElseGet(() -> ApiResponse.error("No se pudo actualizar. Usuario no encontrado"));
    }

    @Override
    public ApiResponse<String> cambiarEstadoUsuario(int id, boolean nuevoEstado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstado(nuevoEstado);
            if (!nuevoEstado) {
                usuario.setSituacion(SituacionUsuario.INACTIVO);
            } else {
                usuario.setSituacion(determinarSituacionPorRol(usuario.getRol()));
            }
            usuarioRepository.save(usuario);
            String mensaje = nuevoEstado ? "Usuario activado correctamente" : "Usuario desactivado correctamente";
            return ApiResponse.success(mensaje, mensaje);
        }).orElseGet(() -> ApiResponse.error("Usuario no encontrado"));
    }

    private SituacionUsuario determinarSituacionPorRol(Rol rol) {
        if (rol == Rol.TRANSPORTISTA || rol == Rol.OPERARIO || rol == Rol.COORDINADOR) {
            return SituacionUsuario.DISPONIBLE;
        }
        return SituacionUsuario.NINGUNO;
    }

    private SuperUsuarioResponse mapearA_Response(UsuarioEntity usuario) {
        return SuperUsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .celular(usuario.getCelular())
                .username(usuario.getUsername())
                .dni(usuario.getDni())
                .estado(usuario.isEstado())
                .rol(usuario.getRol())
                .situacion(usuario.getSituacion())
                .build();
    }
}
