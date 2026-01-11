package uz.fido.pfexchange.dto.mip.push;

import jakarta.validation.constraints.NotNull;

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
public class MipPushPensionRequestDataDto {

    private Long wsId;
    @NotNull private String pinfl;
    @NotNull private String type;
    @NotNull private String grounds;
}
