package uz.fido.pfexchange.dto.mip.push;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipPushRequestDto<T> {

    private String correlationId;
    private T data;
    private LocalDateTime dtl;
    private List<String> destinationSubscribers;
}
