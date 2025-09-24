package uz.fido.pfexchange.config;

public interface Constants {
    interface Error {
        String PRECONDITION_FAILED = "precondition_failed";
        String UNAUTHORIZED = "unauthorized";
        String FORBIDDEN = "forbidden";
        String MISSING_FIELD = "missing_field";
        String INVALID_DATA = "invalid_data";
        String LIMIT_EXCEEDED = "limit_exceeded";
        String NOT_FOUND = "not_found";
        String SYSTEM_ERROR = "system_error";
    }
}
