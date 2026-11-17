package org.ecotrack_project.service.Implement;

import org.ecotrack_project.dto.request.LoginRequest;
import org.ecotrack_project.dto.request.UsuarioRequest;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.entity.UsuarioEntity;
import org.ecotrack_project.enums.Rol;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.repository.UsuarioRepository;
import org.ecotrack_project.security.JwtService;
import org.ecotrack_project.security.UsuarioDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void registrarCuentaGuardaUsuarioConPasswordEncriptadaYRolDeUsuario() {
        UsuarioRequest request = UsuarioRequest.builder()
                .nombre("Ana")
                .apellido("Perez")
                .dni("12345678")
                .correo("ana@example.com")
                .celular("987654321")
                .username("ana.perez")
                .password("ClaveSegura123")
                .build();

        when(usuarioRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(invocation -> {
            UsuarioEntity usuario = invocation.getArgument(0);
            usuario.setIdUsuario(42);
            return usuario;
        });

        ApiResponse<Integer> response = usuarioService.registrarCuenta(request);

        ArgumentCaptor<UsuarioEntity> usuarioCaptor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        UsuarioEntity usuarioGuardado = usuarioCaptor.getValue();

        assertTrue(response.isSuccess());
        assertEquals(42, response.getData());
        assertEquals("hash-seguro", usuarioGuardado.getPassword());
        assertEquals("ana.perez", usuarioGuardado.getUsername());
        assertEquals(Rol.USUARIO, usuarioGuardado.getRol());
        assertEquals(SituacionUsuario.NINGUNO, usuarioGuardado.getSituacion());
        assertTrue(usuarioGuardado.isEstado());
        verify(passwordEncoder).encode("ClaveSegura123");
    }

    @Test
    void registrarCuentaRechazaUsernameDuplicado() {
        UsuarioRequest request = UsuarioRequest.builder()
                .username("ana.perez")
                .correo("ana@example.com")
                .password("ClaveSegura123")
                .build();
        when(usuarioRepository.existsByUsername(request.getUsername())).thenReturn(true);

        ApiResponse<Integer> response = usuarioService.registrarCuenta(request);

        assertFalse(response.isSuccess());
        assertEquals("El username ya está registrado - Digite otro", response.getMessage());
        assertNull(response.getData());
        verify(usuarioRepository, never()).existsByCorreo(any());
        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registrarCuentaRechazaCorreoDuplicado() {
        UsuarioRequest request = UsuarioRequest.builder()
                .username("ana.perez")
                .correo("ana@example.com")
                .password("ClaveSegura123")
                .build();
        when(usuarioRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(true);

        ApiResponse<Integer> response = usuarioService.registrarCuenta(request);

        assertFalse(response.isSuccess());
        assertEquals("El correo ya está registrado - Digite otro", response.getMessage());
        assertNull(response.getData());
        verify(usuarioRepository, never()).save(any(UsuarioEntity.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void obtenerTokenAutenticaCredencialesYDevuelveJwt() {
        LoginRequest request = LoginRequest.builder()
                .username("ana.perez")
                .password("ClaveSegura123")
                .build();
        UsuarioEntity usuario = UsuarioEntity.builder()
                .idUsuario(42)
                .username(request.getUsername())
                .password("hash-seguro")
                .rol(Rol.USUARIO)
                .estado(true)
                .build();
        UsuarioDetails usuarioDetails = new UsuarioDetails(usuario);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuarioDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generarToken(usuarioDetails)).thenReturn("jwt-de-prueba");

        ApiResponse<String> response = usuarioService.obtenerToken(request);

        assertTrue(response.isSuccess());
        assertEquals("jwt-de-prueba", response.getData());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generarToken(usuarioDetails);
    }

    @Test
    void obtenerTokenRechazaCredencialesInvalidas() {
        LoginRequest request = LoginRequest.builder()
                .username("ana.perez")
                .password("incorrecta")
                .build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        ApiResponse<String> response = usuarioService.obtenerToken(request);

        assertFalse(response.isSuccess());
        assertEquals("Username o password incorrectos, o usuario inactivo", response.getMessage());
        assertNull(response.getData());
        verify(jwtService, never()).generarToken(any());
    }
}
