package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model;

import lombok.Data;

import java.util.Date;

@Data
public class StatsPerDay {
    private String date;
    private long count;

}
