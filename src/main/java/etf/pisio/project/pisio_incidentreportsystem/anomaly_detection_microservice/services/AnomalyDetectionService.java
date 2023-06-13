package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.services;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.ReportInfoDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.ReportInfo;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.WarningDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.Warning;

import java.util.List;
public interface AnomalyDetectionService {
    List<WarningDTO> searchForAnomalies();
    void detect(ReportInfoDTO reportInfoDTO);
    void addReport(ReportInfoDTO reportInfoDTO);
}
