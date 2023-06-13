package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.ReportInfo;
import lombok.Data;

import java.util.*;

@Data
public class WarningDTO {
    private long id;
    private String level;
    private Date date;
    private String ident;
    private double x;
    private double y;
    private double r;
    private List<ReportInfoDTO> reports;
    public WarningDTO(long id){
        this.id=id;
    }
    public WarningDTO(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarningDTO that = (WarningDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
