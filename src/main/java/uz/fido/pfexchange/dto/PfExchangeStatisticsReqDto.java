package uz.fido.pfexchange.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "data")
public class PfExchangeStatisticsReqDto {

    @NotBlank
    @Size(min = 7, max = 7, message = "Davr kodi uzunligi noto'g'ri")
    private String period;

    @NotBlank
    @Size(min = 2, max = 14, message = "Coato kodi uzunligi noto'g'ri")
    private String coato;
}
