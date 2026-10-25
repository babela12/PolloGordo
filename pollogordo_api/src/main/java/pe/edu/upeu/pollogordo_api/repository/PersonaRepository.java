package pe.edu.upeu.pollogordo_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.pollogordo_api.entity.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    boolean existsByDni(String dni);
}