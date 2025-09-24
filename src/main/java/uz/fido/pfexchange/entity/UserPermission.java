package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Date;

@Entity
@Table(name = "core_exch_user_permissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPermission implements GrantedAuthority {

    @Id
    @Column(name = "user_permission_id")
    private Integer userPermissionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_code")
    private Permission permission;

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

    @Override
    public String getAuthority() {
        return permission.getCode();
    }
}
