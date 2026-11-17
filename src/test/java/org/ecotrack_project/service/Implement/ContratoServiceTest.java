package org.ecotrack_project.service.Implement;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContratoServiceTest {

    private final ContratoService service = new ContratoService();

    @Test
    void generarContratoDevuelveUnPdfNoVacio() throws Exception {
        byte[] pdf = service.generarContrato(
                "Administradora",
                "Cliente",
                "SOL-15",
                "Retiro de materiales reciclables",
                new BigDecimal("25.00"),
                new BigDecimal("100.00"),
                "16/11/2026",
                null
        );

        assertTrue(pdf.length > 4);
        assertEquals("%PDF", new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
    }
}
