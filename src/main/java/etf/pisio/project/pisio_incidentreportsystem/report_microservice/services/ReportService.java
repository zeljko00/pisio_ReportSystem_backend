package etf.pisio.project.pisio_incidentreportsystem.report_microservice.services;

import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportTypeDTO;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    boolean approval(long id,boolean approval);
    boolean delete(long id);
    List<ReportDTO> find(Boolean approved,String dateExp, String type, String subtype,String address);
    Optional<ReportDTO> create(ReportDTO report);
    List<ReportTypeDTO> getTypes();
    boolean saveImage(byte[] data, String id);
    void deleteImage(String id);
    byte[] getImageById(long id);
}
