package uz.fido.pfexchange.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PF_S_EXCHANGE_STATUSES")
public class PfExchangeStatuses {
    
    @Id
    @Column(name = "code", length = 2)
    private String code;
    
    @Column(name = "name_1", nullable = false, length = 128)
    private String name1;
    
    @Column(name = "name_2", length = 128)
    private String name2;
    
    @Column(name = "name_3", length = 128)
    private String name3;
}