package uz.fido.pfexchange.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import uz.fido.pfexchange.entity.User;

@Slf4j
public class Utils {

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    static {
        XML_MAPPER.configure(
            ToXmlGenerator.Feature.WRITE_XML_DECLARATION,
            false
        );
    }

    public static UserDetails getUserDetails() {
        try {
            return (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        } catch (Exception e) {
            log.error(e.getMessage());
            return new User();
        }
    }

    public static void makeSurePaginationExistence(Map<String, String> params) {
        if (Objects.nonNull(params)) {
            try {
                Integer.parseInt(params.get("page"));
            } catch (Exception ignored) {
                params.put("page", "0");
            }
            try {
                int size = Integer.parseInt(params.get("size"));
                if (size == 0) throw new IllegalAccessException();
            } catch (Exception ignored) {
                params.put("size", "10");
            }
        }
    }

    public static String normalizeDateString(@NonNull String date) {
        String[] parts = date.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                "Invalid date format. Expected dd.MM.yyyy"
            );
        }

        String day = parts[0].toLowerCase().contains("x") ? "01" : parts[0];
        String month = parts[1].toLowerCase().contains("x") ? "01" : parts[1];
        String year = parts[2].toLowerCase().contains("x") ? "2000" : parts[2];

        return String.format("%s.%s.%s", day, month, year);
    }

    public static LocalDate convertToLocalDate(Object date) {
        if (date == null) return null;
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (date instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (date instanceof LocalDate localDate) {
            return localDate;
        }
        throw new IllegalArgumentException(
            "Unexpected date type: " + date.getClass()
        );
    }
}
