package uz.fido.pfexchange.dto.minyust;

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
public class ProcessingResult {

    private boolean success;
    private String message;

    public static ProcessingResult success() {
        return new ProcessingResult(true, null);
    }

    public static ProcessingResult failure(String message) {
        return new ProcessingResult(false, message);
    }
}
