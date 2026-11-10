package org.ecotrack_project.service.Implement;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContratoService {

    public byte[] generarContrato(
            String administrador,
            String cliente,
            String idSolicitud,
            String descripcion,
            BigDecimal pagoInicial,
            BigDecimal pagoTotal,
            String fechaFinalizacion,
            byte[] firmaAdministrador
    ) throws Exception {

        ClassPathResource recurso = new ClassPathResource(
                "reports/contrato_servicio_ecotrack.jrxml"
        );

        try (InputStream plantilla = recurso.getInputStream()) {

            JasperReport reporte = JasperCompileManager.compileReport(plantilla);

            Map<String, Object> parametros = new HashMap<>();

            parametros.put("administrador", administrador);
            parametros.put("cliente", cliente);
            parametros.put("idSolicitud", idSolicitud);
            parametros.put("descripcion", descripcion);
            parametros.put("pagoInicial", pagoInicial);
            parametros.put("pagoTotal", pagoTotal);
            parametros.put("fechaFinalizacion", fechaFinalizacion);

            parametros.put(
                    "firmaAdministrador",
                    firmaAdministrador != null
                            ? new ByteArrayInputStream(firmaAdministrador)
                            : null
            );

            JasperPrint documento = JasperFillManager.fillReport(
                    reporte,
                    parametros,
                    new JREmptyDataSource(1)
            );

            return JasperExportManager.exportReportToPdf(documento);
        }
    }
}