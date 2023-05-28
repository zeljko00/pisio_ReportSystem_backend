package etf.pisio.project.pisio_incidentreportsystem.DTO;

import etf.pisio.project.pisio_incidentreportsystem.model.Coordinate;
import etf.pisio.project.pisio_incidentreportsystem.model.ReportImage;
import jakarta.persistence.*;
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
