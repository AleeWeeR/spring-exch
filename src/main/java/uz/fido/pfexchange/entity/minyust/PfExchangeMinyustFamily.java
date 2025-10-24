package uz.fido.pfexchange.entity.minyust;

import jakarta.persistence.*;
import lombok.*;
import uz.fido.pfexchange.entity.PfExchangeStatuses;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PF_EXCHANGE_MINYUST_FAMILY")
public class PfExchangeMinyustFamily {
    
    @Id
    @Column(name = "query_family_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "query_family_seq")
    @SequenceGenerator(name = "query_family_seq", sequenceName = "Pf_Exchange_Minyust_Family_Sq", allocationSize = 1)
    private Long queryFamilyId;
    
    @Column(name = "pinpp", length = 14)
    private String pinpp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status", nullable = false)
    private PfExchangeStatuses status;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;
    
    @Column(name = "data_sqlerr", length = 4000)
    private String dataSqlerr;
    
    @Column(name = "data_in", length = 4000)
    private String dataIn;
    
    @Column(name = "surname", length = 50)
    private String surname;
    
    @Column(name = "name", length = 50)
    private String name;
    
    @Column(name = "patronym", length = 50)
    private String patronym;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "is_manual", length = 1)
    private String isManual;
}