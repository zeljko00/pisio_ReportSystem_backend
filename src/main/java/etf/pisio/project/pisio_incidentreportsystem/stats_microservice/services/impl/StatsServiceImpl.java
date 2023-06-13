package etf.pisio.project.pisio_incidentreportsystem.stats_microservice.services.impl;

import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model.Stats;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model.StatsPerAddress;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model.StatsPerDay;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.model.StatsPerType;
import etf.pisio.project.pisio_incidentreportsystem.stats_microservice.services.StatsService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Transactional
@Service
public class StatsServiceImpl implements StatsService {
    public Stats getStats(List<ReportDTO> reports){
        if(reports==null)
            return null;
        Stats stats=new Stats();
        stats.setCount(reports.size());
        stats.setApproved(reports.stream().filter(r -> r.isApproved()).count());
        if(stats.getCount()!=0)
            stats.setApprovedPercentage(stats.getApproved()/(double)stats.getCount());
        else
            stats.setApprovedPercentage(0);
        stats.setReportsPerType(new ArrayList<>());
        stats.setDataPerDay(new ArrayList<>());
        stats.setDataPerAddress(new ArrayList<>());
        reports.stream().collect(Collectors.groupingBy(r -> r.getType())).entrySet().stream().forEach(entry -> {
            StatsPerType statsPerType=new StatsPerType();
            statsPerType.setType(entry.getKey());
            statsPerType.setCount(entry.getValue().size());
            statsPerType.setApproved(entry.getValue().stream().filter(r -> r.isApproved()).count());
            statsPerType.setPercentage(statsPerType.getCount()/(double)stats.getCount());
            stats.getReportsPerType().add(statsPerType);
        });
        DateFormat df=new SimpleDateFormat("yyyy.MM.dd");
        reports.stream().collect(Collectors.groupingBy(r -> df.format(r.getDate()))).entrySet().stream().forEach(entry -> {
            StatsPerDay statsPerDay=new StatsPerDay();
            statsPerDay.setDate(entry.getKey());
            statsPerDay.setCount(entry.getValue().size());
            stats.getDataPerDay().add(statsPerDay);
        });
        reports.stream().collect(Collectors.groupingBy(r -> r.getAddress())).entrySet().stream().sorted((e1,e2)-> {return e2.getValue().size()-e1.getValue().size();}).limit(4).forEach(entry -> {
            StatsPerAddress statsPerAddress=new StatsPerAddress();
            statsPerAddress.setAddress(entry.getKey());
            statsPerAddress.setCount(entry.getValue().size());
            statsPerAddress.setApproved(entry.getValue().stream().filter(i -> i.isApproved()).count());
            stats.getDataPerAddress().add(statsPerAddress);
        });
        try{
            Date start=reports.stream().map(r -> {return r.getDate();}).max((d1, d2) -> {return d1.compareTo(d2);}).get();
            Date end=reports.stream().map(r -> {return r.getDate();}).min((d1, d2) -> {return d1.compareTo(d2);}).get();
            long days= TimeUnit.DAYS.convert(Math.abs(end.getTime()-start.getTime()), TimeUnit.MILLISECONDS);
            if(days==0)
                days=1;
            stats.setAvgPerDay(stats.getCount()/(double)days);
        }catch (Exception e){e.printStackTrace();}
        return stats;
    }
}
