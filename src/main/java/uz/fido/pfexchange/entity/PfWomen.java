package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.fido.pfexchange.utils.MinyustFamilyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pf_women")
public class PfWomen {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "pinpp", nullable = false, length = 14)
    private String pinpp;

    @Column(name = "application_id", nullable = false, length = 15)
    private Long applicationId;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Lob
    @Column(name = "data_in", length = 4000)
    private String dataIn;

    @Lob
    @Column(name = "data_err", length = 4000)
    private String dataErr;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MinyustFamilyStatus status;

}
