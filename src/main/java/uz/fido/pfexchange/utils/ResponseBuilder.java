package uz.fido.pfexchange.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {
    public static ResponseEntity<?> ok() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
    public static ResponseEntity<?> ok(Object data) {
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
    public static ResponseEntity<?> get(Object data, HttpStatus status) {
        return new ResponseEntity<>(data, status);
    }
    public static ResponseEntity<?> get(HttpStatus status) {
        return new ResponseEntity<>(status);
    }

}
