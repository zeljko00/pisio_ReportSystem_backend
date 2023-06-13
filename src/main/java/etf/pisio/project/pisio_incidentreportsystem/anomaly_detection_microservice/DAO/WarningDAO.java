package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DAO;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
public interface WarningDAO extends JpaRepository<Warning,Long> {
    List<Warning> findWarningsByDateAfter(Date date);
}
