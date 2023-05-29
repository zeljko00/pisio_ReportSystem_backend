package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model;

import lombok.Data;

import java.util.List;
@Data
public class Stats {
    private long count;
    private long approved;
    private int approvedPercentage;
    private List<StatsPerType> reportsPerType;
    private List<StatsPerInterval> dataPerInterval;
    private double reportsPerInterval;
    private double approvedPerInterval;
}
