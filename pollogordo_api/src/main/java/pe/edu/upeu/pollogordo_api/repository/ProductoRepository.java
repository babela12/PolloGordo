package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
