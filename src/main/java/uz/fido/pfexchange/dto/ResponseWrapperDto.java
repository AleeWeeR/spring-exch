package uz.fido.pfexchange.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "root")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "root", description = "Standard API response wrapper")
public class ResponseWrapperDto <T> {
    private Integer code;
    private String message;
    private T data;
}