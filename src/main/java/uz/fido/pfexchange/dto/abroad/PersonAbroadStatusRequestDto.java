package uz.fido.pfexchange.dto.abroad;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Person abroad status so'rovi uchun DTO
 * Request DTO for person abroad status query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "Request")
public class PersonAbroadStatusRequestDto {


    @Valid
    @NotNull
    @JsonProperty("Data")
    private PersonAbroadStatusDataDto data;

}
