package etf.pisio.project.pisio_incidentreportsystem.DAO;

import etf.pisio.project.pisio_incidentreportsystem.model.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageDAO extends JpaRepository<ReportImage,Long> {
}
