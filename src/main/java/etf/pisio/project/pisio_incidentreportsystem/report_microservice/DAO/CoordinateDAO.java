package etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO;

import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinateDAO extends JpaRepository<Coordinate,Long> {
}
