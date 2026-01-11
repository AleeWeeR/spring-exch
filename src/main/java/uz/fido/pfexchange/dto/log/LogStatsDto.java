package uz.fido.pfexchange.dto.log;

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
public class LogStatsDto {

    private long totalRequests;
    private long errorCount;
    private long avgDurationMs;
    private int periodHours;
}
