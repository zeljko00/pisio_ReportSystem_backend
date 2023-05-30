package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.services;

import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model.Stats;

import java.util.List;

public interface StatsService {
    Stats getStats(List<ReportDTO> reports);
}
