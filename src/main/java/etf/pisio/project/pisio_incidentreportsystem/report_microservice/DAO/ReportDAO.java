package etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO;

import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportDAO extends JpaRepository<Report,Long> {
    @Query (value="SELECT report FROM Report report WHERE (:date IS NULL OR report.date >= :date) AND (:type IS NULL OR report.type LIKE :type) AND (:subtype IS NULL OR report.type LIKE :subtype)" +
           " AND (:address IS NULL OR lower(report.address) like lower(concat('%',:address,'%'))) AND (:approval IS NULL OR report.approved = :approval)")
    List<Report> findReportsByApproval(@Param("approval") Boolean approval,@Param("date") Date date, @Param("type")String type,@Param("subtype")String subtype,@Param("address") String address);
}
