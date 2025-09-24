package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PF_S_EXCHANGE_STATUSES")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PfSExchangeStatus {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "name_1")
    private String name1;

    @Column(name = "name_2")
    private String name2;

    @Column(name = "name_3")
    private String name3;

}
