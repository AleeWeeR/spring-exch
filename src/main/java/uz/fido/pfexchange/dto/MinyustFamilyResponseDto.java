package uz.fido.pfexchange.dto;

import lombok.*;

import java.util.List;

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
