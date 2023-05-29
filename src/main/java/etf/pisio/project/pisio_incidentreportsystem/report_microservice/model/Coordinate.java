package etf.pisio.project.pisio_incidentreportsystem.report_microservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Coordinate {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;
    private double x;
    private double y;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn ( name="reportId", referencedColumnName = "id",nullable = false)
    private Report report;
}
