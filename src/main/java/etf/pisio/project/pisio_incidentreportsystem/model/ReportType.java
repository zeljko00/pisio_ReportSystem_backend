package etf.pisio.project.pisio_incidentreportsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class ReportType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @OneToMany (mappedBy = "type",fetch = FetchType.LAZY)
    private List<Subtype> subtypes;
}
