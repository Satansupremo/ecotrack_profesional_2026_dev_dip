package org.ecotrack_project.service.Implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ecotrack_project.entity.DetalleEntity;
import org.ecotrack_project.enums.SituacionUsuario;
import org.ecotrack_project.enums.SituacionVehiculo;
import org.ecotrack_project.repository.DetalleRepository;

import java.util.List;

@Service
public class LiberacionRecursosService {

    private final DetalleRepository detalleRepository;

    public LiberacionRecursosService(DetalleRepository detalleRepository) {
        this.detalleRepository = detalleRepository;
    }

    @Transactional
    public void liberarRecursos(int idProgramacion) {

        List<DetalleEntity> detalles = detalleRepository
                .findByProgramacion_IdProgramacionOrderByIdDetalleAsc(idProgramacion);

        for (DetalleEntity detalle : detalles) {
            detalle.getTransportista().setSituacion(SituacionUsuario.DISPONIBLE);
            detalle.getVehiculo().setSituacion(SituacionVehiculo.DISPONIBLE);
        }
    }
}