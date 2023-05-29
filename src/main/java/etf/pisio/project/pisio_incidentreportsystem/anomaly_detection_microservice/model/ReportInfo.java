package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Data;

import java.util.Date;
import java.util.Objects;
import java.util.List;

@Data
@Entity
public class ReportInfo {
    @Id
    private long id;
    private Date date;
    private String type;
    private String address;
    private double x;
    private double y;
    @ManyToMany (mappedBy = "reports")
    private List<Warning> warnings;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportInfo that = (ReportInfo) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
