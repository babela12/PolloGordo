
package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import pe.edu.upeu.pollogordo_api.dto.ProductoDTO;
import pe.edu.upeu.pollogordo_api.services.IFileStorageService;
import pe.edu.upeu.pollogordo_api.services.IProductoService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final IProductoService productoService;
    private final IFileStorageService storageService;

    public ProductoController(
            IProductoService productoService,
            IFileStorageService storageService) {

        this.productoService = productoService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarTodo() {

        List<ProductoDTO> lista = productoService.listarTodo();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Long id) {

        Optional<ProductoDTO> producto = productoService.buscarPorId(id);

        if (producto.isPresent()) {
            return ResponseEntity.ok(producto.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoDTO> crear(
            @Valid @ModelAttribute ProductoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String imagen = "no-image.png";

        if (file != null && !file.isEmpty()) {
            imagen = storageService.store(file);
        }

        ProductoDTO producto = new ProductoDTO(
                null,
                dto.nombre(),
                dto.descripcion(),
                dto.tipo(),
                dto.precio(),
                dto.estado(),
                imagen
        );

        ProductoDTO nuevo = productoService.crear(producto);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductoDTO> actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        Optional<ProductoDTO> resultado = productoService.buscarPorId(id);

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProductoDTO actual = resultado.get();

        String imagen = actual.imagen();

        if (file != null && !file.isEmpty()) {
            imagen = storageService.store(file);
        }

        ProductoDTO producto = new ProductoDTO(
                id,
                dto.nombre(),
                dto.descripcion(),
                dto.tipo(),
                dto.precio(),
                dto.estado(),
                imagen
        );

        Optional<ProductoDTO> actualizado =
                productoService.actualizar(id, producto);

        if (actualizado.isPresent()) {
            return ResponseEntity.ok(actualizado.get());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        boolean eliminado = productoService.eliminar(id);

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
