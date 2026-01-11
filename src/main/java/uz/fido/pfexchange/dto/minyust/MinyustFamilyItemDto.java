package uz.fido.pfexchange.dto.minyust;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.*;
import uz.fido.pfexchange.utils.FlexibleLocalDateDeserializer;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MinyustFamilyItemDto {

    private String m_pnfl;
    private String f_family;
    private String gender_code;
    private String cert_series;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate birth_date;

    private String f_pnfl;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate doc_date;

    private String m_first_name;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate cert_birth_date;

    private String surname;
    private String m_patronym;
    private String pnfl;
    private String f_first_name;
    private String cert_number;
    private String f_patronym;
    private String patronym;
    private String m_family;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate f_birth_day;

    private String name;
    private String live_status;
    private String branch;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate m_birth_day;

    private String doc_num;
}
