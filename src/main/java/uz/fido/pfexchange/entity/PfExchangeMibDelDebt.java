package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pf_exchange_mib_del_debt")
@Getter
@Setter
public class PfExchangeMibDelDebt {

    @Id
    @Column(name = "mib_del_debt_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mib_del_debt_seq")
    @SequenceGenerator(name = "mib_del_debt_seq", sequenceName = "pf_exchange_mib_del_debt_s", allocationSize = 1)
    private Long mibDelDebtId;

    @Column(name = "external_id")
    private Long externalId;

    @Column(name = "canceled_by_pf_id")
    private Long canceledByPfId;

    @Column(name = "pinpp", length = 14)
    private String pinpp;

    @Column(name = "debt_amount")
    private Double debtAmount;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}
