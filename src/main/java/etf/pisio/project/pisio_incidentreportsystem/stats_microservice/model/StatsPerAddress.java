package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model;

import lombok.Data;

@Data
public class StatsPerAddress {
    private String address;
    private long count;
    private long approved;
}
