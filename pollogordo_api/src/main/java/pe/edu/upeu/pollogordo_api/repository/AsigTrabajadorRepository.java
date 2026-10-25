
package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.AsigTrabajador;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsigTrabajadorRepository
        extends JpaRepository<AsigTrabajador, Long> {

    // Buscar todas las asignaciones de un trabajador
    List<AsigTrabajador> findByTrabajador_IdTrabajador(
            Long idTrabajador
    );

    // Buscar asignaciones de un trabajador en una fecha
    List<AsigTrabajador> findByTrabajador_IdTrabajadorAndFechaTrabajo(
            Long idTrabajador,
            LocalDate fechaTrabajo
    );

    // Buscar asignaciones por pedido
    List<AsigTrabajador> findByPedido_IdPedido(
            Long idPedido
    );

    // Buscar asignaciones por fecha de trabajo
    List<AsigTrabajador> findByFechaTrabajo(
            LocalDate fechaTrabajo
    );

    // Buscar asignaciones por trabajador, fecha y función
    List<AsigTrabajador>
    findByTrabajador_IdTrabajadorAndFechaTrabajoAndFuncion(
            Long idTrabajador,
            LocalDate fechaTrabajo,
            String funcion
    );
}
