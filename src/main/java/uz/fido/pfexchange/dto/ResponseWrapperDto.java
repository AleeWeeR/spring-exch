package uz.fido.pfexchange.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "root")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseWrapperDto {
    private Integer code;
    private String message;
    private Object data;
}