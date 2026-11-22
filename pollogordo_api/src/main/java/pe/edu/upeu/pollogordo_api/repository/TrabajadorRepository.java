
package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.Trabajador;

import java.util.Optional;

@Repository
public interface TrabajadorRepository
        extends JpaRepository<Trabajador, Long> {

    boolean existsByPersona_IdPersona(Long idPersona);

    Optional<Trabajador> findByPersona_IdPersona(Long idPersona);

}
