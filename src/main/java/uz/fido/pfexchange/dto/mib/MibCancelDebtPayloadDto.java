package uz.fido.pfexchange.dto.mib;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO for MIB pension API cancel-inventory request payload
 * This is sent to https://pension.mib.uz/cancel-inventory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MibCancelDebtPayloadDto {

    @JsonProperty("inventory_id")
    private Long inventoryId;

    @JsonProperty("fio_performer")
    private String fioPerformer;

    @JsonProperty("phone_performer")
    private String phonePerformer;

    @JsonProperty("reason_id")
    private Integer reasonId;

    @JsonProperty("reason_name")
    private String reasonName;
}
