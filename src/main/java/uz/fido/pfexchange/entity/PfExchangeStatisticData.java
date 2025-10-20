package uz.fido.pfexchange.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Blob;

@Entity
@Table(name = "pf_exchange_statistic_files")
@Getter
@Setter
public class PfExchangeStatisticData {

    @EmbeddedId
    private OrgPeriodKey id;

    @MapsId("organizationId")
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Lob
    private Blob json;
}
