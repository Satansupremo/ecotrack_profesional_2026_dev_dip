
package org.ecotrack_project.controller;

import lombok.RequiredArgsConstructor;
import org.ecotrack_project.dto.response.ApiResponse;
import org.ecotrack_project.dto.response.SolicitudDetalleResponse;
import org.ecotrack_project.initialize.Initialize;
import org.ecotrack_project.service.Implement.ContratoService;
import org.ecotrack_project.service.SolicitudService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/contrato")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;
    private final SolicitudService solicitudService;

    @GetMapping("/generar/{idSolicitud}")
    public ResponseEntity<?> generarContrato(
            @PathVariable int idSolicitud
    ) throws Exception {

        ApiResponse<SolicitudDetalleResponse> response =
                solicitudService.obtenerSolicitud(idSolicitud);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(response.getMessage()));
        }

        SolicitudDetalleResponse solicitud = response.getData();

        if (solicitud.getEstado() == null
                || !"FINALIZADO".equals(solicitud.getEstado())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(
                            "No se puede generar el contrato porque la solicitud no ha finalizado."
                    ));
        }

        if (solicitud.getPagoInicial() == null
                || solicitud.getPagoTotal() == null
                || solicitud.getFechaFinalizacion() == null
                || solicitud.getPagoInicial().getMonto() == null
                || solicitud.getPagoTotal().getMonto() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(
                            "No se puede generar el contrato porque faltan datos necesarios de la solicitud."
                    ));
        }

        try {
            ClassPathResource firmaRecurso = new ClassPathResource(
                    "static/firma.png"
            );

            byte[] firmaAdministrador;

            try (InputStream inputStream = firmaRecurso.getInputStream()) {
                firmaAdministrador = inputStream.readAllBytes();
            }

            byte[] pdf = contratoService.generarContrato(
                    Initialize.ADMINISTRADOR,
                    solicitud.getNombreUsuario() + " " + solicitud.getApellidoUsuario(),
                    String.valueOf(solicitud.getIdSolicitud()),
                    solicitud.getDescripcion(),
                    solicitud.getPagoInicial().getMonto(),
                    solicitud.getPagoTotal().getMonto(),
                    solicitud.getFechaFinalizacion().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    ),
                    firmaAdministrador
            );

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=contrato_ecotrack.pdf"
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Ocurrió un error al generar el contrato."
                    ));
        }
    }
}
