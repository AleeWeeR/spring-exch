package uz.fido.pfexchange.dto.diagnostic;

import java.util.List;
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
public class DiagnosticResponse {
    private boolean success;
    private String requestedUrl;
    private String requestMethod;
    private String timestamp;
    
    private Map<String, String> dnsResolution;
    
    private Integer httpStatusCode;
    private Map<String, List<String>> responseHeaders;
    private String responseBody;
    
    private Long requestDurationMs;
    private Long totalDurationMs;
    
    private String errorType;
    private String errorMessage;
    private Map<String, String> errorDetails;
    private String overallMessage;
}