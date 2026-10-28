
package org.ecotrack_project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public String generarToken(UserDetails userDetails) {
        UsuarioDetails usuarioDetails = (UsuarioDetails) userDetails;

        Date fechaActual = new Date();
        Date fechaExpiracion = new Date(fechaActual.getTime() + expiration);

        return Jwts.builder()
                .subject(usuarioDetails.getUsername())
                .claim("idUsuario", usuarioDetails.getUsuario().getIdUsuario())
                .claim("rol", usuarioDetails.getUsuario().getRol().name())
                .issuedAt(fechaActual)
                .expiration(fechaExpiracion)
                .signWith(getSigningKey())
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public boolean validarToken(
            String token,
            UserDetails userDetails) {

        String username = extraerUsername(token);

        return username.equals(userDetails.getUsername())
                && extraerClaim(token, Claims::getExpiration)
                .after(new Date())
                && userDetails.isEnabled();
    }

    private <T> T extraerClaim(
            String token,
            Function<Claims, T> resolver) {

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }
}
