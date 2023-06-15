package etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.services.impl;

import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DAO.ReportInfoDAO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DAO.WarningDAO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.ReportInfoDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.DTO.WarningDTO;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.ReportInfo;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.Warning;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.model.WarningLevel;
import etf.pisio.project.pisio_incidentreportsystem.anomaly_detection_microservice.services.AnomalyDetectionService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private static final int STORAGE_PERIOD_DAYS = 7;
    private static final double CRITICAL_RADIUS = 0.01;
    private static final int CRITICAL_COUNT = 4;
    private static final int EARTH_R=6371000;
    private final ReportInfoDAO reportInfoDAO;
    private final WarningDAO warningDAO;

    private ModelMapper modelMapper;

    public AnomalyDetectionServiceImpl(ReportInfoDAO reportInfoDAO, WarningDAO warningDAO, ModelMapper modelMapper) {
        this.reportInfoDAO = reportInfoDAO;
        this.warningDAO = warningDAO;
        this.modelMapper = modelMapper;
    }

    public List<WarningDTO> searchForAnomalies() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, (-1 * STORAGE_PERIOD_DAYS));

        List<WarningDTO> warns= warningDAO.findWarningsByDateAfter(calendar.getTime()).stream().map(w -> {
            WarningDTO dto = modelMapper.map(w, WarningDTO.class);
            dto.setReports(w.getReports().stream().map(r -> {
                return modelMapper.map(r, ReportInfoDTO.class);
            }).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
        List<Long> warnsToRemove=new ArrayList<>();
        for(WarningDTO w1: warns){
            for(WarningDTO w2: warns){
                if(w1.getId()!=w2.getId() && w1.getIdent().contains(w2.getIdent())){
                    if(warnsToRemove.contains(w2.getId())==false){
                        warnsToRemove.add(w2.getId());
                        System.out.println("Removed warn with id="+w2.getId());
                    }
                }
            }
        }
        warnsToRemove.stream().forEach(l -> warns.remove(new WarningDTO(l)));
        return warns;
    }
    public void addReport(ReportInfoDTO reportInfoDTO){
        ReportInfo report = modelMapper.map(reportInfoDTO, ReportInfo.class);
        reportInfoDAO.save(report);

        detect(reportInfoDTO);
    }
    public void detect(ReportInfoDTO report){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, (-1 * STORAGE_PERIOD_DAYS));

        List<ReportInfo> reports = reportInfoDAO.findAllByDateAfter(calendar.getTime());
        List<ReportInfo> nearReports = reports.stream().filter(r -> {
            return Math.abs(r.getX() - report.getX()) <= CRITICAL_RADIUS && Math.abs(r.getY() - report.getY()) <= CRITICAL_RADIUS;
        }).collect(Collectors.toList());

        Map<String, Warning> warnings = new HashMap<>();

        for (ReportInfo ri : nearReports) {
            List<ReportInfo> list = new ArrayList<>();
            list.add(ri);
            double dist = 0;
            for (ReportInfo r : reports) {
                if (r.getId() != ri.getId()) {
                    if (Math.abs(r.getX() - report.getX()) <= CRITICAL_RADIUS && Math.abs(r.getY() - report.getY()) <= CRITICAL_RADIUS) {
                        list.add(r);
                        double dLat1=r.getX()*Math.PI/180;
                        double dLat2=ri.getX()*Math.PI/180;
                        double dLat=(r.getX()-ri.getX())*Math.PI/180;
                        double dLng=(r.getY()-ri.getY())*Math.PI/180;
                        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                                Math.cos(dLat1) * Math.cos(dLat2) *
                                        Math.sin(dLng/2) * Math.sin(dLng/2);
                        double c= 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                        double l=EARTH_R*c;
//                        double l = Math.sqrt(Math.pow(ri.getX() - r.getX(), 2) + Math.pow(ri.getY() - r.getY(), 2));
                        if (l > dist)
                            dist = l;
                    }
                }
            }
            if (list.size() >= CRITICAL_COUNT) {

                List<ReportInfo> temp = list.stream().sorted((r1, r2) -> {
                            return (int) (r1.getId() - r2.getId());
                        }
                ).collect(Collectors.toList());
                String ident = temp.stream().map(rep -> {
                    return "#" + rep.getId();
                }).reduce("", (s1, s2) -> {
                    return s1 + s2;
                });
                if (!warnings.containsKey(ident)) {
                    Warning anomaly = new Warning();
                    anomaly.setReports(list);
                    anomaly.setX(ri.getX());
                    anomaly.setY(ri.getY());
                    anomaly.setR(dist);
                    anomaly.setIdent(ident);
                    warnings.put(ident, anomaly);
                }
            }
        }
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
        }
    }

}
