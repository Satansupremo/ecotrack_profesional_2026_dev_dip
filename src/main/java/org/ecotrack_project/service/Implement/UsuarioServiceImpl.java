
package org.ecotrack_project.service.Implement;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.security.JwtService;
import org.ecotrack_project.security.UsuarioDetails;
import org.ecotrack_project.service.UsuarioService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
                    "Autenticación exitosa"
            );

        } catch (AuthenticationException exception) {
            return ApiResponse.error(
                    "Username o password incorrectos, o usuario inactivo"
            );
        }
    }

    @Override
    public ApiResponse<UsuarioEntity> obtenerUsuarioLogueado() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof UsuarioDetails usuarioDetails)) {

            return ApiResponse.error(
                    "No hay un usuario autenticado"
            );
        }

        return ApiResponse.success(
                usuarioDetails.getUsuario(),
                "Usuario obtenido correctamente"
        );
    }
}
