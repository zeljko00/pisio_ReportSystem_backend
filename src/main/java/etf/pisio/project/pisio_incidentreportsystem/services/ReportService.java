package etf.pisio.project.pisio_incidentreportsystem.services;

import etf.pisio.project.pisio_incidentreportsystem.DTO.CoordinateDTO;
import etf.pisio.project.pisio_incidentreportsystem.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.model.Coordinate;
import etf.pisio.project.pisio_incidentreportsystem.model.Report;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReportService {
    boolean approval(long id,boolean approval);
    boolean delete(long id);
    List<ReportDTO> find(Boolean approved,String dateExp, String type, String subtype,String address,Double x, Double y, Double radius);
    Optional<ReportDTO> create(ReportDTO report);
}
