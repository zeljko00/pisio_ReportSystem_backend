package etf.pisio.project.pisio_incidentreportsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ReportImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="reportId", referencedColumnName = "id", nullable = false)
    private Report report;
}
