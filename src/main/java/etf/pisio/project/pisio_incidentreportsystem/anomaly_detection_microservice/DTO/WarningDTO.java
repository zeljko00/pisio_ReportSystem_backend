package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.ReportInfo;
import lombok.Data;

import java.util.*;

@Data
public class WarningDTO {
    private Long id;
    private String level;
    private Date date;
    private double x;
    private double y;
    private double r;
    private List<ReportInfoDTO> reports;
}
