package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "core_exchange_permissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permission {

    @Id
    @Column(name = "code")
    private String code;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "is_active_flag")
    private String isActiveFlag;

    @Column(name = "add_info")
    private String addInfo;

    @Column(nullable = false, name = "created_by")
    private String createdBy;

    @Column(nullable = false, name = "creation_date", columnDefinition = "date default sysdate")
    private Date creationDate;

    @Column(nullable = false, name = "last_updated_by")
    private String lastUpdatedBy;

    @Column(nullable = false, name = "last_update_date", columnDefinition = "date default sysdate")
    private Date lastUpdateDate;
}
