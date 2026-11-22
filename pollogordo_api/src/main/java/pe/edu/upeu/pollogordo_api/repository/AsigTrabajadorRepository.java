package pe.edu.upeu.pollogordo_api.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.AsigTrabajador;
import java.time.LocalDate;
import java.util.List;
@Repository
public interface AsigTrabajadorRepository extends JpaRepository<AsigTrabajador, Long> {
    List<AsigTrabajador> findByTrabajador_IdTrabajador(
            Long idTrabajador
    );
    List<AsigTrabajador> findByTrabajador_IdTrabajadorAndFechaTrabajo(
            Long idTrabajador, LocalDate fechaTrabajo
    );
    List<AsigTrabajador> findByPedido_IdPedido(
            Long idPedido
    );
    List<AsigTrabajador> findByPedido_IdPedidoAndFuncion(
            Long idPedido, String funcion
    );
    List<AsigTrabajador> findByFechaTrabajo(
            LocalDate fechaTrabajo
    );
    List<AsigTrabajador> findByTrabajador_IdTrabajadorAndFechaTrabajoAndFuncion(
            Long idTrabajador, LocalDate fechaTrabajo, String funcion
    );
}