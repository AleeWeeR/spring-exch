package uz.fido.pfexchange.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
        String dateStr = p.getText();

        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        // Handle XX placeholders
        String normalizedDate = normalizeDate(dateStr.trim());

        return LocalDate.parse(normalizedDate, FORMATTER);
    }

    private String normalizeDate(String dateStr) {
        String[] parts = dateStr.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                "Invalid date format: " + dateStr
            );
        }

        String day = parts[0];
        String month = parts[1];
        String year = parts[2];

        // Replace XX with default values
        if (day.equalsIgnoreCase("XX")) {
            day = "01";
        }
        if (month.equalsIgnoreCase("XX")) {
            month = "01";
        }
        if (year.equalsIgnoreCase("XX") || year.equalsIgnoreCase("XXXX")) {
            year = "1900";
        }

        return day + "." + month + "." + year;
    }
}
