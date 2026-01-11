package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "os_organizations")
@Getter
@Setter
public class Organization {

    @Id
    @Column(name = "organization_id")
    private Integer organizationId;

    @Column(name = "name_2")
    private String name;

    @Column(name = "address_code")
    private String coato;

    @Column(name = "is_active_flag")
    private String active;
}
