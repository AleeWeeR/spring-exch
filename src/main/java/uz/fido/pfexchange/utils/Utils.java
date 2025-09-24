package uz.fido.pfexchange.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import uz.fido.pfexchange.entity.User;

import java.util.Map;
import java.util.Objects;

public class Utils {
    private static final Logger _logger = LogManager.getLogger(Utils.class);
    public static UserDetails getUserDetails() {
        try {
            return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            _logger.fatal(e);
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
                if (size == 0)
                    throw new IllegalAccessException();
            } catch (Exception ignored) {
                params.put("size", "10");
            }
        }
    }
}
