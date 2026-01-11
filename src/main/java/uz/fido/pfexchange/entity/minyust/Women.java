package uz.fido.pfexchange.entity.minyust;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import uz.fido.pfexchange.utils.MinyustFamilyStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pf_women")
public class Women {

    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "pinpp", nullable = false, length = 14)
    private String pinpp;

    @Column(name = "application_id", nullable = false, length = 15)
    private Long applicationId;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data_in")
    private String dataIn;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data_err")
    private String dataErr;

    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MinyustFamilyStatus status;
}
