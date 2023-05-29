package etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO;

import lombok.Data;

import java.util.Date;
import java.util.List;
@Data
public class ReportDTO {

    private long id;
    private Date date;
    private String content;
    private String type;
    private String address;
    private boolean approved;
    private double x;
    private double y;
    private List<Long> imagesIDs;
    private List<CoordinateDTO> coordinateDTOs;
}
