package uz.fido.pfexchange.dto.mip.paytype;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import uz.fido.pfexchange.annotation.validation.ValidPensType;
import uz.fido.pfexchange.utils.validation.FirstCheck;
import uz.fido.pfexchange.utils.validation.SecondCheck;
import uz.fido.pfexchange.utils.validation.ThirdCheck;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipPayTypeDataDto {

    @NotNull(groups = FirstCheck.class)
    @Digits(integer = 12, fraction = 0)
    private Long wsId;

    @Size(min = 14, max = 14)
    @NotBlank(groups = FirstCheck.class)
    @Pattern(regexp = "\\d{14}", message = "{pinfl.pattern}", groups = SecondCheck.class)
    private String pinfl;

    @Size(min = 2, max = 2)
    @NotBlank(groups = FirstCheck.class)
    @ValidPensType(groups = ThirdCheck.class)
    @Pattern(regexp = "\\d{2}", message = "{pens.type.pattern}", groups = SecondCheck.class)
    private String pensType;
}
