package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pf_mib_cancel_rel")
@Getter
@Setter
public class PfMibCancelRel {

    @Id
    @Column(name = "mib_cancel_rel_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mib_cancel_rel_seq")
    @SequenceGenerator(name = "mib_cancel_rel_seq", sequenceName = "pf_mib_cancel_rel_s", allocationSize = 1)
    private Long mibCancelRelId;

    @Column(name = "external_id")
    private Long externalId;

    @Column(name = "pinpp", length = 14)
    private String pinpp;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "is_sent", length = 1)
    private String isSent;

    @Column(name = "is_cancelled", length = 1)
    private String isCancelled;

    @Column(name = "comment_text", length = 1000)
    private String commentText;

    @Column(name = "data_out", length = 4000)
    private String dataOut;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_updated_by")
    private Long lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}
