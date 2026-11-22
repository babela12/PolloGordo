
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class EvidenciaPagoService {

    private final Path directorio;

    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024;

    private static final Set<String> TIPOS =
            Set.of("image/jpeg", "image/png", "application/pdf");

    public EvidenciaPagoService(
            @Value("${app.pagos.evidencias-dir:uploads/pagos}")
            String ruta) throws IOException {

        directorio = Paths.get(ruta).toAbsolutePath().normalize();
        Files.createDirectories(directorio);
    }

    public String guardar(MultipartFile archivo) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un comprobante"
            );
        }

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El comprobante no puede superar los 5 MB"
            );
        }

        String tipo = archivo.getContentType();

        if (tipo == null || !TIPOS.contains(tipo.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Solo se permiten archivos JPG, PNG o PDF"
            );
        }

        String extension = switch (tipo.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".pdf";
        };

        String nombre = UUID.randomUUID() + extension;
        Path destino = directorio.resolve(nombre).normalize();

        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(
                    entrada,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return nombre;
    }

    public Path obtener(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El comprobante no existe"
            );
        }

        Path archivo = directorio.resolve(nombre).normalize();

        if (!archivo.startsWith(directorio)
                || !Files.isRegularFile(archivo)) {
            throw new IllegalArgumentException(
                    "No se encontró el comprobante"
            );
        }

        return archivo;
    }

    public void eliminar(String nombre) throws IOException {

        if (nombre != null && !nombre.isBlank()) {
            Files.deleteIfExists(obtener(nombre));
        }
    }
}
