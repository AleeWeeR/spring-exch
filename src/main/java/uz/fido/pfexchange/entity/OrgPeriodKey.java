package uz.fido.pfexchange.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class OrgPeriodKey implements Serializable {

    @Column(name = "organization_id")
    private Integer organizationId;

    @Column(name = "period_code")
    private String period;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrgPeriodKey that)) return false;
        return Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, period);
    }
}
