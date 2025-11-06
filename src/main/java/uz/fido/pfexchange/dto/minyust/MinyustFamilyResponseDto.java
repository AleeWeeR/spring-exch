package uz.fido.pfexchange.dto.minyust;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MinyustFamilyResponseDto {

    private String result_message;
    private String result_code;
    private String id;
    private List<MinyustFamilyItemDto> items;
}
