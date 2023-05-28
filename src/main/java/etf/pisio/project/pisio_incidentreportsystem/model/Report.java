package etf.pisio.project.pisio_incidentreportsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
public class Report {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;
    private Date date;
    private String content;
    private String type;
    private String address;
    private boolean approved;
    private double x;
    private double y;
    @OneToMany (mappedBy = "report", fetch=FetchType.LAZY)
    private List<ReportImage> images;
    @OneToMany (mappedBy = "report", fetch=FetchType.LAZY)
    private List<Coordinate> coordinates;
}
