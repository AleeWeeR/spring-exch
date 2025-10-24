package uz.fido.pfexchange.dto.minyust;

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

}
