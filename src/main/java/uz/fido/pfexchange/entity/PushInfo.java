package uz.fido.pfexchange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.fido.pfexchange.utils.PushStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Pf_Exchange_Mip_Push_Info")
public class PushInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pf_exchange_mip_push_info_gen")
    @SequenceGenerator(
            name = "pf_exchange_mip_push_info_gen",
            sequenceName = "Pf_Exchange_Mip_Push_Info_Sq",
            allocationSize = 1)
    @Column(name = "ws_id")
    private Long wsId;

    private String correlationId;

    @Column(name = "pinpp")
    private String pinpp;

    @Column(name = "pens_type")
    private String pensType;

    @Column(name = "grounds")
    private String grounds;

    @Builder.Default
    @Column(name = "request_date")
    private LocalDateTime requestDate = LocalDateTime.now();
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PushStatus status;
}
