package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.controllers;

import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model.Stats;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.services.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping
    public ResponseEntity<Stats> stats(@RequestBody List<ReportDTO> reports) {
        Stats result = statsService.getStats(reports);
        if (result != null)
            return new ResponseEntity<>(result, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
