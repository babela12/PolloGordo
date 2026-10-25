package pe.edu.upeu.pollogordo_api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.pollogordo_api.dto.ProductoDTO;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.services.IFileStorageService;
import pe.edu.upeu.pollogordo_api.services.IProductoService;

import java.util.List;

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
        return ResponseEntity.ok(productoService.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ProductoDTO> crear(
            @ModelAttribute
            @org.springdoc.core.annotations.ParameterObject
            @Valid ProductoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String nombreImagen = "no-image.png";

        if (file != null && !file.isEmpty()) {
            nombreImagen = storageService.store(file);
        }

        ProductoDTO productoConImagen = new ProductoDTO(
                null,
                dto.nombre(),
                dto.descripcion(),
                dto.tipo(),
                dto.precio(),
                dto.estado(),
                nombreImagen
        );

        return new ResponseEntity<>(
                productoService.crear(productoConImagen),
                HttpStatus.CREATED
        );
    }

    @PutMapping(
            value = "/{id}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<ProductoDTO> actualizar(
            @PathVariable Long id,
            @ModelAttribute
            @org.springdoc.core.annotations.ParameterObject
            @Valid ProductoDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        ProductoDTO productoActual = productoService.buscarPorId(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto no encontrado"));

        String nombreImagen = productoActual.imagen();

        if (file != null && !file.isEmpty()) {
            nombreImagen = storageService.store(file);
        }

        ProductoDTO dtoParaActualizar = new ProductoDTO(
                id,
                dto.nombre(),
                dto.descripcion(),
                dto.tipo(),
                dto.precio(),
                dto.estado(),
                nombreImagen
        );

        return productoService.actualizar(id, dtoParaActualizar)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (productoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        throw new ResourceNotFoundException(
                "No se pudo eliminar: " + id);
    }
}