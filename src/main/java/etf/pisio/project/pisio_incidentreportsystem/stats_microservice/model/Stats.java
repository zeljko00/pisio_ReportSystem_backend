package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model;

import lombok.Data;

import java.util.List;
@Data
public class Stats {
    private long count;
    private long approved;
    private double approvedPercentage;
    private List<StatsPerType> reportsPerType;
    private List<StatsPerDay> dataPerDay;
    private List<StatsPerAddress> dataPerAddress;
    private double avgPerDay;
}
