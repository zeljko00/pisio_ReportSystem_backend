package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DAO;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.ReportInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.*;

@Repository
public interface ReportInfoDAO extends JpaRepository<ReportInfo,Long> {
    List<ReportInfo> findAllByDateAfter(Date date);
}
