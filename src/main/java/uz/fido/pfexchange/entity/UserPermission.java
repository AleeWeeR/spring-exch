package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "core_exch_user_permissions")
public class UserPermission implements GrantedAuthority {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "user_permission_seq"
    )
    @SequenceGenerator(
        name = "user_permission_seq",
        sequenceName = "core_exch_user_permissions_sq",
        allocationSize = 1
    )
    @Column(name = "user_permission_id")
    private Long userPermissionId;

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

    @Builder.Default
    @Column(nullable = false, name = "created_by")
    private Long createdBy = 1L;

    @Builder.Default
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false, name = "last_updated_by")
    private Long lastUpdatedBy = 1L;

    @Builder.Default
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "last_update_date")
    private LocalDateTime lastUpdateDate = LocalDateTime.now();

    @Override
    public String getAuthority() {
        return permission.getCode();
    }
}
