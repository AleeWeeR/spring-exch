package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "core_exchange_permissions")
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

    @Builder.Default
    @Column(nullable = false, name = "created_by")
    private Long createdBy = 1L;

    @Builder.Default
    @Column(
        nullable = false,
        name = "creation_date",
        columnDefinition = "date default sysdate"
    )
    private LocalDateTime creationDate = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false, name = "last_updated_by")
    private Long lastUpdatedBy = 1L;

    @Builder.Default
    @Column(
        nullable = false,
        name = "last_update_date",
        columnDefinition = "date default sysdate"
    )
    private LocalDateTime lastUpdateDate = LocalDateTime.now();
}
