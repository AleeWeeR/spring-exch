package uz.fido.pfexchange.dto.minyust;

import java.util.Map;
import lombok.*;
import uz.fido.pfexchange.utils.MinyustFamilyBatchStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinyustFamilyBatchResponseDto {

    private MinyustFamilyBatchStatus status;
    private String message;
    private Long pendingCount;
    private Map<String, Long> statusSummary;
}
