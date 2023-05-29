package etf.pisio.project.pisio_incidentreportsystem.report_microservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Data
@Entity
public class Subtype {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn ( name="typeId", referencedColumnName = "id", nullable = false)
    private ReportType type;

    public Subtype(String name){
        this.name=name;
    }
    public Subtype(){}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subtype subtype = (Subtype) o;
        return Objects.equals(name, subtype.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
