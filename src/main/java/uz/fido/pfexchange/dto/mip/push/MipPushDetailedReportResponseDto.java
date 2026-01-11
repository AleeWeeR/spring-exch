package uz.fido.pfexchange.dto.mip.push;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipPushDetailedReportResponseDto {

    private String correlationId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedDateTime;
    private List<MipPushDeliveriesDto> deliveries;
}
