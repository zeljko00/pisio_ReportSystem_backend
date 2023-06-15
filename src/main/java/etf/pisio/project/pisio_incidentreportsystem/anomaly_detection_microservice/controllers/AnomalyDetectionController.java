package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.controllers;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.ReportInfoDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.WarningDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.services.AnomalyDetectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anomaly_detection")
public class AnomalyDetectionController {
    private final AnomalyDetectionService anomalyDetectionService;
    @Value("service.secretKey")
    private String secret;

    public AnomalyDetectionController(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @PostMapping
    ResponseEntity<?> addReport(@RequestBody ReportInfoDTO report, @RequestHeader(HttpHeaders.AUTHORIZATION) String key) {
        if (key.equals(secret)) {
            System.out.println("Received valid key!");
            anomalyDetectionService.addReport(report);
            return new ResponseEntity<>(HttpStatus.OK);
        } else{
            System.out.println(key+" != "+secret);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping
    public ResponseEntity<List<WarningDTO>> search() {
        return new ResponseEntity<>(anomalyDetectionService.searchForAnomalies(), HttpStatus.OK);
    }
}
