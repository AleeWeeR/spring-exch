package uz.fido.pfexchange.dto.diagnostic;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {
    private String url;
    private String method = "GET";
    private Map<String, String> headers;
    private String body;
}