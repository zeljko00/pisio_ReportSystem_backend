package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.controllers;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.ReportInfoDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.ReportInfo;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.WarningDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.Warning;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.services.AnomalyDetectionService;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anomaly_detection")
public class AnomalyDetectionController {
    private final AnomalyDetectionService anomalyDetectionService;

    public AnomalyDetectionController(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }
    @PostMapping
    ResponseEntity<?> addReport(@RequestBody ReportInfoDTO report){
        anomalyDetectionService.addReport(report);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<WarningDTO>> search(){
        return new ResponseEntity<>(anomalyDetectionService.searchForAnomalies(),HttpStatus.OK);
    }
}
