package uz.fido.pfexchange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "pf_s_exchange_doc_types")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocTypes {

    @Id
    @Column(name = "code")
    private String code;

    @Column(nullable = false, name = "name_1")
    private String name1;

    @Column(name = "name_2")
    private String name2;

    @Column(name = "name_3")
    private String name3;

    @Column(nullable = false, name = "is_active_flag")
    private String isActiveFlag;

    @Column(nullable = false, name = "path")
    private String path;

    @Column(nullable = false, name = "dir")
    private String dir;

}
