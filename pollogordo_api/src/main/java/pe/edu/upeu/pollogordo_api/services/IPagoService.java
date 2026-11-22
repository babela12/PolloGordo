
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.pollogordo_api.dto.PagoDTO;
import pe.edu.upeu.pollogordo_api.dto.ResumenPagoDTO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface IPagoService {

    List<PagoDTO> listarTodo();

    Optional<PagoDTO> buscarPorId(Long id);

    PagoDTO crear(PagoDTO pago);

    Optional<PagoDTO> actualizar(Long id, PagoDTO pago);

    boolean eliminar(Long id);

    List<PagoDTO> listarPorPedido(Long idPedido);

    ResumenPagoDTO obtenerResumen(Long idPedido);

    PagoDTO subirEvidencia(Long id, MultipartFile archivo) throws IOException;

    Path obtenerEvidencia(Long id);
}
