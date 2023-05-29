package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model;

import lombok.Data;
import java.util.List;
@Data
public class Anomaly {
    private List<ReportInfo> reports;
    private double x;
    private double y;
    private double radius;
}
