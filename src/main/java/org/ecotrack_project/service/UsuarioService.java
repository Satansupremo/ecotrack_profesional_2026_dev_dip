
package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.entity.UsuarioEntity;
import org.springframework.stereotype.Service;

@Service
public interface UsuarioService {

    ApiResponse<String> obtenerToken(LoginRequest loginRequest);
    ApiResponse<UsuarioEntity> obtenerUsuarioLogueado();
}
