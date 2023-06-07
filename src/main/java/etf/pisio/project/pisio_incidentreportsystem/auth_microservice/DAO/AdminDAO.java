package etf.pisio.project.pisio_incidentreportsystem.auth_microservice.DAO;

import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDAO extends JpaRepository<Admin,Long> {
}
