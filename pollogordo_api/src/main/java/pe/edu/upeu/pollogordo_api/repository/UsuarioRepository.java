
package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.pollogordo_api.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByUsuario(String usuario);

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByPersona_IdPersona(Long idPersona);

}
