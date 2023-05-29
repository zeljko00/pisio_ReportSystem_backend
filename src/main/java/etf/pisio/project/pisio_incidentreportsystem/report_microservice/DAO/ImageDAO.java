package etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO;

import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageDAO extends JpaRepository<ReportImage,Long> {
}
