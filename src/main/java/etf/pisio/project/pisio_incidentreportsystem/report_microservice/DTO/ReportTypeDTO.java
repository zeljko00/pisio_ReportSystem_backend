package etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ReportTypeDTO {
    private String name;
    private List<String> subtypes;
}
