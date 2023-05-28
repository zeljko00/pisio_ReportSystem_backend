package etf.pisio.project.pisio_incidentreportsystem.controllers;

import etf.pisio.project.pisio_incidentreportsystem.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.services.ReportService;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    ResponseEntity<List<ReportDTO>> getApprovedReports(@PathParam("dateExp") String dateExp, @PathParam("address") String address, @PathParam("type") String type, @PathParam("subtype") String subtype, @PathParam("x") Double x, @PathParam("y") Double y, @PathParam("radius") Double radius){
        return new ResponseEntity<>(reportService.find(true,dateExp,type,subtype,address,x,y,radius),HttpStatus.OK);
    }
    @GetMapping("/queue")
    ResponseEntity<List<ReportDTO>> getReports(@PathParam("approval") Boolean approval,@PathParam("dateExp") String dateExp, @PathParam("address") String address, @PathParam("type") String type, @PathParam("subtype") String subtype, @PathParam("x") Double x, @PathParam("y") Double y, @PathParam("radius") Double radius){
        return new ResponseEntity<>(reportService.find(approval,dateExp,type,subtype,address,x,y,radius),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable("id") long id){
        boolean flag=reportService.delete(id);
        if(flag)
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> approval(@PathVariable("id") long id, @PathParam("approval") boolean approval){
        boolean flag=reportService.approval(id,approval);
        if(flag)
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PostMapping
    ResponseEntity<ReportDTO> create(@RequestBody ReportDTO report){
        Optional<ReportDTO> opt=reportService.create(report);
        if(opt.isPresent())
            return new ResponseEntity<>(opt.get(),HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
