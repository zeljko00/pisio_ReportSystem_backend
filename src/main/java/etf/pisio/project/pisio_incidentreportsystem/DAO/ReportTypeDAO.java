package etf.pisio.project.pisio_incidentreportsystem.DAO;

import etf.pisio.project.pisio_incidentreportsystem.model.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportTypeDAO extends JpaRepository<ReportType,Long> {
    Optional<ReportType> findByName(String name);
}
