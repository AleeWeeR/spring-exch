package uz.fido.pfexchange.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto implements Serializable {

    private String tokenType;
    private Integer expiresIn;
    private String accessToken;
}
