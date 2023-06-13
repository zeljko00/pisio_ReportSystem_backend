package etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO;

import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportImageDAO extends JpaRepository<ReportImage,Long> {
    @Modifying
    @Query("delete from ReportImage image where image.report.id=:id")
    void deleteAllByReportId(@Param("id") long id);
}
