package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model;

import lombok.Data;

@Data
public class StatsPerType {
    private String type;
    private long count;
    private double percentage;
}
