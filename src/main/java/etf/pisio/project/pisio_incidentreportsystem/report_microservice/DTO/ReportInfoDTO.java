package etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO;

import lombok.Data;

import java.util.Date;

@Data
public class ReportInfoDTO {
    private long id;
    private Date date;
    private String type;
    private String address;
    private double x;
    private double y;
}
