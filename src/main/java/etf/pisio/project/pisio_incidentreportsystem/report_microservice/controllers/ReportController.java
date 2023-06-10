package etf.pisio.project.pisio_incidentreportsystem.report_microservice.controllers;

import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportTypeDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.ReportService;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    ResponseEntity<List<ReportDTO>> getApprovedReports(@PathParam("dateExp") String dateExp, @PathParam("address") String address, @PathParam("type") String type, @PathParam("subtype") String subtype){
        return new ResponseEntity<>(reportService.find(true,dateExp,type,subtype,address),HttpStatus.OK);
    }
    @GetMapping("/queue")
    ResponseEntity<List<ReportDTO>> getReports(@PathParam("approval") Boolean approval,@PathParam("dateExp") String dateExp, @PathParam("address") String address, @PathParam("type") String type, @PathParam("subtype") String subtype){
        return new ResponseEntity<>(reportService.find(approval,dateExp,type,subtype,address),HttpStatus.OK);
    }
    @GetMapping("/types")
    ResponseEntity<List<ReportTypeDTO>> getReportTypes(){
        return new ResponseEntity<>(reportService.getTypes(),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable("id") long id){
        System.out.println("deleting report with id "+id);
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
    @PostMapping("/images/upload")
    public void uploadImage(@RequestParam("image") MultipartFile file, @RequestParam("identificator") String id) throws IOException {
        reportService.saveImage(file.getBytes(),id);
    }
    @DeleteMapping("/images/{id}")
    public ResponseEntity<?> deleteReportImageById(@PathVariable("id") String id){
        reportService.deleteImage(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping(path="/images/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> getReportImageById(@PathVariable("id") long id){
        byte[] result=reportService.getImageById(id);
        if(result!=null)
            return new ResponseEntity<>(result,HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
