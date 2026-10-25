package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.Distribucion;

import java.util.List;

@Repository
public interface DistribucionRepository
        extends JpaRepository<Distribucion, Long> {

    List<Distribucion> findByPedidoIdPedido(Long idPedido);
}