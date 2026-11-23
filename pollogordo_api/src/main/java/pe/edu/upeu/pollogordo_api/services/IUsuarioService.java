
package pe.edu.upeu.pollogordo_api.services;

import pe.edu.upeu.pollogordo_api.dto.ActualizarCuentaDTO;
import pe.edu.upeu.pollogordo_api.dto.RegistroDTO;
import pe.edu.upeu.pollogordo_api.dto.UsuarioDTO;

import java.util.Optional;

public interface IUsuarioService {

    Optional<UsuarioDTO> registrarUsuario(RegistroDTO dto);

    UsuarioDTO actualizarCuenta(
            Long idUsuario,
            ActualizarCuentaDTO dto
    );
}
