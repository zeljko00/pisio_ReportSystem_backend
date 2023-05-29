package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.services.impl;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DAO.ReportInfoDAO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DAO.WarningDAO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.ReportInfoDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.WarningDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.Anomaly;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.ReportInfo;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.Warning;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.WarningLevel;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.services.AnomalyDetectionService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private static final int STORAGE_PERIOD_DAYS = 7;
    private static final double CRITICAL_RADIUS = 0.01;
    private static final int CRITICAL_COUNT = 4;
    private final ReportInfoDAO reportInfoDAO;
    private final WarningDAO warningDAO;

    private ModelMapper modelMapper;

    public AnomalyDetectionServiceImpl(ReportInfoDAO reportInfoDAO, WarningDAO warningDAO, ModelMapper modelMapper) {
        this.reportInfoDAO = reportInfoDAO;
        this.warningDAO = warningDAO;
        this.modelMapper = modelMapper;
    }

    public List<WarningDTO> history(String period) {
        Date date = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (period != null) {
            switch (period) {
                case "24h":
                    calendar.add(Calendar.DATE, -1);
                    date = calendar.getTime();
                    break;
                case "7d":
                    calendar.add(Calendar.DATE, -7);
                    date = calendar.getTime();
                    break;
                case "1m":
                    calendar.add(Calendar.MONTH, -1);
                    date = calendar.getTime();
                    break;
            }
        }
        List<WarningDTO> result = new ArrayList<>();
        if (date != null)
            result = warningDAO.findWarningsByDateBefore(date).stream().map(w -> {
                WarningDTO dto = modelMapper.map(w, WarningDTO.class);
                dto.setReports(w.getReports().stream().map(r -> {
                    return modelMapper.map(r, ReportInfoDTO.class);
                }).collect(Collectors.toList()));
                return dto;
            }).collect(Collectors.toList());
        else
            result = warningDAO.findAll().stream().map(w -> {
                WarningDTO dto = modelMapper.map(w, WarningDTO.class);
                dto.setReports(w.getReports().stream().map(r -> {
                    return modelMapper.map(r, ReportInfoDTO.class);
                }).collect(Collectors.toList()));
                return dto;
            }).collect(Collectors.toList());
        return result;
    }

    public List<WarningDTO> searchForAnomalies(ReportInfoDTO reportDTO) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, (-1 * STORAGE_PERIOD_DAYS));
        reportInfoDAO.deleteReportInfoByDateBefore(calendar.getTime());
        ReportInfo report=modelMapper.map(reportDTO,ReportInfo.class);
        reportInfoDAO.save(report);

        List<ReportInfo> reports = reportInfoDAO.findAll();
        List<ReportInfo> nearReports = reports.stream().filter(r -> {
            return Math.abs(r.getX() - report.getX()) <= CRITICAL_RADIUS && Math.abs(r.getY() - report.getY()) <= CRITICAL_RADIUS;
        }).collect(Collectors.toList());

        Map<String, Warning> warnings = new HashMap<>();

        for (ReportInfo ri : nearReports) {
            List<ReportInfo> list = new ArrayList<>();
            list.add(ri);
            double dist=CRITICAL_RADIUS;
            for (ReportInfo r : reports) {
                if (r.getId() != ri.getId()) {
                    if (Math.abs(r.getX() - report.getX()) <= CRITICAL_RADIUS && Math.abs(r.getY() - report.getY()) <= CRITICAL_RADIUS){
                        list.add(r);
                        double l=Math.sqrt(Math.pow(ri.getX()-r.getX(),2)+Math.pow(ri.getY()-r.getY(),2));
                        if(l<dist)
                            dist=l;
                    }
                }
            }
            if (list.size() >= CRITICAL_COUNT && list.contains(report)) {

                List<ReportInfo> temp = list.stream().sorted((r1, r2) -> {
                            return (int) (r1.getId() - r2.getId());
                        }
                ).collect(Collectors.toList());
                String ident = temp.stream().map(rep -> {
                    return "#" + rep.getId() + "  ";
                }).reduce("", (s1, s2) -> {
                    return s1 + s2;
                });
                if (!warnings.containsKey(ident)){
                    Warning anomaly=new Warning();
                    anomaly.setReports(list);
                    anomaly.setX(ri.getX());
                    anomaly.setY(ri.getY());
                    anomaly.setR(dist);
                    warnings.put(ident, anomaly);
                }
            }
        }

        List<WarningDTO> result = new ArrayList<WarningDTO>();
        for (
                Map.Entry<String, Warning> entry : warnings.entrySet()) {
            Warning warning = entry.getValue();
            warning.setLevel(WarningLevel.LOW.toString());
            warning.setDate(new Date());
            Map<String, List<ReportInfo>> groups = entry.getValue().getReports().stream().collect(Collectors.groupingBy(repInfo -> repInfo.getType()));
            long count = groups.entrySet().stream().filter(en -> en.getValue().size() >= CRITICAL_COUNT).count();
            if (count > 0)
                warning.setLevel(WarningLevel.HIGH.toString());
            warning = warningDAO.saveAndFlush(warning);

            WarningDTO warningDTO = modelMapper.map(warning, WarningDTO.class);
            warningDTO.setReports(warning.getReports().stream().map(rep-> {
                return modelMapper.map(rep,ReportInfoDTO.class);
            }).collect(Collectors.toList()));
            result.add(warningDTO);
        }
        return result;
    }
}
