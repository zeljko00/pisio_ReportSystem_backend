package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
public class Warning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String level;
    private Date date;
    private String ident;
    private double x;
    private double y;
    private double r;
    @ManyToMany
    @JoinTable(
            name = "anomaly_reports",
            joinColumns = @JoinColumn(name = "warning_id"),
            inverseJoinColumns = @JoinColumn(name = "reportInfo_id"))
    private List<ReportInfo> reports;
}
