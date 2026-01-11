package uz.fido.pfexchange.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "core_exchange_users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_exchange_seq")
    @SequenceGenerator(
            name = "user_exchange_seq",
            sequenceName = "core_exchange_users_sq",
            allocationSize = 1)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false, name = "login")
    private String username;

    @JsonIgnore
    @Column(nullable = false, name = "encrypted_password")
    private String password;

    @Builder.Default
    @Column(nullable = false, name = "change_password_date")
    private LocalDate changePasswordDate = LocalDate.of(3090, 1, 1);

    @Builder.Default
    @Column(nullable = false, name = "change_password_flag", length = 1)
    private String changePasswordFlag = "N";

    @Builder.Default
    @Column(nullable = false, name = "is_active_flag")
    private String isActiveFlag = "Y";

    @Column(nullable = false, name = "name")
    private String name;

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

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private Set<UserPermission> userPermissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.getUserPermissions().stream()
                .filter(p -> p.getIsActiveFlag().equals("Y"))
                .toList();
    }
}
