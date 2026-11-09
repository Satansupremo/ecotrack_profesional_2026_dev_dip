
package org.ecotrack_project.service.Implement;

import org.ecotrack_project.entity.ProgramacionEntity;
import org.ecotrack_project.entity.SolicitudEntity;
import org.ecotrack_project.enums.EstadoCarga;
import org.ecotrack_project.enums.EstadoProgramacion;
import org.ecotrack_project.enums.EstadoSolicitud;
import org.ecotrack_project.repository.CargaRepository;
import org.ecotrack_project.repository.ColaRepository;
import org.ecotrack_project.repository.ProgramacionRepository;
import org.ecotrack_project.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FinalizacionProgramacionService {

    private final ProgramacionRepository programacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final ColaRepository colaRepository;
    private final CargaRepository cargaRepository;
    private final LiberacionRecursosService liberacionRecursosService;

    public FinalizacionProgramacionService(
            ProgramacionRepository programacionRepository,
            SolicitudRepository solicitudRepository,
            ColaRepository colaRepository,
            CargaRepository cargaRepository,
            LiberacionRecursosService liberacionRecursosService
    ) {
        this.programacionRepository = programacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.colaRepository = colaRepository;
        this.cargaRepository = cargaRepository;
        this.liberacionRecursosService = liberacionRecursosService;
    }

    @Transactional
    public boolean verificarYFinalizar(int idProgramacion) {

        ProgramacionEntity programacion = programacionRepository
                .findByIdForUpdate(idProgramacion)
                .orElse(null);

        if (programacion == null
                || programacion.getEstado() != EstadoProgramacion.POR_FINALIZAR) {
            return false;
        }

        long totalSalidas = colaRepository
                .countByDetalle_Programacion_IdProgramacion(idProgramacion);

        long salidasPendientes = colaRepository
                .countByDetalle_Programacion_IdProgramacionAndEstadoTrue(
                        idProgramacion
                );

        long totalCargasRegistradas = cargaRepository
                .findByProgramacion_IdProgramacionAndEstado(
                        idProgramacion,
                        EstadoCarga.REGISTRADA
                ).size();

        if (totalSalidas == 0
                || salidasPendientes > 0
                || totalSalidas != totalCargasRegistradas) {
            return false;
        }

        LocalDateTime fechaFinalizacion = LocalDateTime.now();

        programacion.setEstado(EstadoProgramacion.FINALIZADO);
        programacion.setFechaFinalizacion(fechaFinalizacion);

        SolicitudEntity solicitud = programacion.getSolicitud();

        solicitud.setEstado(EstadoSolicitud.FINALIZADO);
        solicitud.setFechaFinalizacion(fechaFinalizacion);

        solicitudRepository.save(solicitud);

        liberacionRecursosService.liberarRecursos(idProgramacion);

        programacionRepository.save(programacion);

        return true;
    }
}
