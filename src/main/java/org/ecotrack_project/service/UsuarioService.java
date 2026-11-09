package org.ecotrack_project.service;

import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.request.SuperUsuarioRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SuperUsuarioResponse;
import org.ecotrack_project.entity.UsuarioEntity;
import java.util.List;

public interface UsuarioService {

    ApiResponse<String> obtenerToken(LoginRequest loginRequest);
    ApiResponse<Integer> registrarCuenta(org.ecotrack_project.dto.request.UsuarioRequest usuarioRequest);
    ApiResponse<UsuarioEntity> obtenerUsuarioLogueado();

    ApiResponse<Integer> obtenerTotalPaginas();
    ApiResponse<List<SuperUsuarioResponse>> listarUsuarios(int pagina);
    ApiResponse<SuperUsuarioResponse> obtenerUsuarioPorId(int id);
    ApiResponse<Integer> crearSuperUsuario(SuperUsuarioRequest request);
    ApiResponse<Integer> actualizarSuperUsuario(int id, SuperUsuarioRequest request);
    ApiResponse<String> cambiarEstadoUsuario(int id, boolean nuevoEstado);
}
