
package pe.edu.upeu.pollogordo_api.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Converter
public class HoraEntregaConverter
        implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalTime hora) {
        if (hora == null) {
            return null;
        }
        return hora.format(FORMATO);
    }

    @Override
    public LocalTime convertToEntityAttribute(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return LocalTime.parse(valor, FORMATO);
    }
}
