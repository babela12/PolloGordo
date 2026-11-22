package pe.edu.upeu.pollogordo_api.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.Entrega;
import java.util.List;
@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    boolean existsByPedido_IdPedido(Long idPedido);
    List<Entrega> findByPedido_IdPedido(Long idPedido);
}
