package uz.fido.pfexchange.entity.minyust;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PF_EXCHANGE_MINYUST_FAMILY_INF")
public class PfExchangeMinyustFamilyInf {
    
    @Id
    @Column(name = "family_inf_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "family_inf_seq")
    @SequenceGenerator(name = "family_inf_seq", sequenceName = "Pf_Exch_Minyust_Family_Inf_Sq", allocationSize = 1)
    private Long familyInfId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_family_id", nullable = false)
    private PfExchangeMinyustFamily queryFamily;
    
    @Column(name = "pinpp", length = 14)
    private String pinpp;
    
    @Column(name = "surname", nullable = false, length = 50)
    private String surname;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "patronym", nullable = false, length = 50)
    private String patronym;
    
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    
    @Column(name = "sex", nullable = false)
    private Integer sex;
    
    @Column(name = "reg_number", nullable = false, length = 25)
    private String regNumber;
    
    @Column(name = "reg_date", nullable = false)
    private LocalDate regDate;
    
    @Column(name = "recv_zags_id", nullable = false)
    private Long recvZagsId;
    
    @Column(name = "certificate_seria", length = 25)
    private String certificateSeria;
    
    @Column(name = "certificate_number", length = 25)
    private String certificateNumber;
    
    @Column(name = "certificate_date")
    private LocalDate certificateDate;
    
    @Column(name = "father_pin", length = 14)
    private String fatherPin;
    
    @Column(name = "father_surname_latin", length = 50)
    private String fatherSurnameLatin;
    
    @Column(name = "father_name_latin", length = 50)
    private String fatherNameLatin;
    
    @Column(name = "father_patronym_latin", length = 50)
    private String fatherPatronymLatin;
    
    @Column(name = "father_birth_date")
    private LocalDate fatherBirthDate;
    
    @Column(name = "mother_pin", length = 14)
    private String motherPin;
    
    @Column(name = "mother_surname_latin", length = 50)
    private String motherSurnameLatin;
    
    @Column(name = "mother_name_latin", length = 50)
    private String motherNameLatin;
    
    @Column(name = "mother_patronym_latin", length = 50)
    private String motherPatronymLatin;
    
    @Column(name = "mother_birth_date")
    private LocalDate motherBirthDate;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;
    
    @Column(name = "is_active", nullable = false, length = 1)
    private String isActive;
    
    @Column(name = "is_alive", length = 1)
    private String isAlive;
}