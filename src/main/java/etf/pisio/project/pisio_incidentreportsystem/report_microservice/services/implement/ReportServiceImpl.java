package etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.CoordinateDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportTypeDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.CoordinateDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.Coordinate;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.Report;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.ReportType;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.Subtype;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.ImageService;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.ReportService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    private final ReportDAO reportDAO;
    private final ReportTypeDAO reportTypeDAO;
    private final CoordinateDAO coordinateDAO;
    private ImageService imageService;
    private final ModelMapper modelMapper;

    private final static String subtype_regex = " - ";
    private final static String LAST_DAY = "24h";
    private final static String LAST_WEEK = "7d";
    private final static String LAST_MONTH = "31d";
    private final static String LAST_HALF_YEAR = "6m";

    private final static String GEOCODING_SERVICE_REQUEST_TEMPLATE = "https://api.opencagedata.com/geocode/v1/json?q=LAT+LNG&key=38be49d94e0f42f9af17d878e8c78303";

    public ReportServiceImpl(ReportDAO reportDAO, ReportTypeDAO reportTypeDAO, CoordinateDAO coordinateDAO, ImageService imageService, ModelMapper modelMapper) {
        this.reportDAO = reportDAO;
        this.reportTypeDAO = reportTypeDAO;
        this.coordinateDAO = coordinateDAO;
        this.imageService = imageService;
        this.modelMapper = modelMapper;
    }

    public boolean approval(long id, boolean approved) {
        Optional<Report> opt = reportDAO.findById(id);
        if (opt.isPresent()) {
            Report report = opt.get();
            report.setApproved(approved);
            reportDAO.saveAndFlush(report);
            return true;
        } else return false;
    }

    public boolean delete(long id) {
        if (reportDAO.findById(id).isPresent()) {
            reportDAO.deleteById(id);
            return true;
        } else return false;
    }

    public List<ReportDTO> find(Boolean approved, String dateExp, String type, String subtype, String address, Double x, Double y, Double radius) {
        System.out.println(approved);
        System.out.println(dateExp);
        System.out.println(type);
        System.out.println(subtype);
        System.out.println(address);
        System.out.println(x);
        System.out.println(y);
        System.out.println(radius);

        Date date = null;

        if (dateExp != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            switch (dateExp) {
                case (LAST_DAY):
                    calendar.add(Calendar.DATE, -1);
                    date = calendar.getTime();
                    break;
                case (LAST_WEEK):
                    calendar.add(Calendar.DATE, -7);
                    date = calendar.getTime();
                    break;
                case (LAST_MONTH):
                    calendar.add(Calendar.DATE, -31);
                    date = calendar.getTime();
                    break;
                case (LAST_HALF_YEAR):
                    calendar.add(Calendar.MONTH, -6);
                    date = calendar.getTime();
                    break;

            }
        }
        List<Report> entities = reportDAO.findReportsByApproval(approved, date, type, subtype, address, x, y, radius);
        return entities.stream().map(r -> {
            ReportDTO dto = modelMapper.map(r, ReportDTO.class);
            dto.setImagesIDs(r.getImages().stream().map(i -> {
                return i.getId();
            }).collect(Collectors.toList()));
            dto.setCoordinateDTOs(r.getCoordinates().stream().map(c -> {
                return modelMapper.map(c, CoordinateDTO.class);
            }).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    public Optional<ReportDTO> create(ReportDTO report) {
        if(report.getCoordinateDTOs()!=null && report.getCoordinateDTOs().size()>0){
            report.setY(report.getCoordinateDTOs().stream().map(c->{return c.getY();}).reduce(0.0,(y1,y2)->{return y1+y2;})/report.getCoordinateDTOs().size());
            report.setX(report.getCoordinateDTOs().stream().map(c->{return c.getX();}).reduce(0.0,(x1,x2)->{return x1+x2;})/report.getCoordinateDTOs().size());
        }

        String type = "";
        String subtype = "";
        if (report.getType().contains(subtype_regex)) {
            String[] tokens = report.getType().split(subtype_regex);
            type = tokens[0];
            subtype = tokens[1];
        } else type = report.getType();

        Optional<ReportType> opt = reportTypeDAO.findByName(type);
        if (opt.isPresent()) {
            ReportType typeEntity = opt.get();
            if (subtype.equals("") || (typeEntity.getSubtypes() != null && typeEntity.getSubtypes().contains(new Subtype(subtype)))) {
                report.setDate(new Date());
                report.setApproved(false);

                String request = GEOCODING_SERVICE_REQUEST_TEMPLATE.replace("LAT", Double.toString(report.getX())).replace("LNG", Double.toString(report.getY()));
                try {
                    URL obj = new URL(request);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(response.toString());
                        if (jsonNode.get("results") != null && jsonNode.get("results").get(0) != null) {
                            report.setAddress(jsonNode.get("results").get(0).get("formatted").asText());
                            System.out.println(report.getAddress());
                        } else report.setAddress("???");
                    } else report.setAddress("???");
                } catch (Exception e) {
                    report.setAddress("???");
                }

                //fotografije

                Report entity = modelMapper.map(report, Report.class);
                List<Coordinate> coords = report.getCoordinateDTOs().stream().map(c -> {
                    return modelMapper.map(c, Coordinate.class);
                }).collect(Collectors.toList());
                entity.setCoordinates(coords);
                Report result = reportDAO.saveAndFlush(entity);
                coords.stream().forEach(c -> {
                    c.setReport(result);
                    coordinateDAO.saveAndFlush(c);
                });

                report.setId(result.getId());
                return Optional.of(report);
            } else
                return Optional.empty();
        } else
            return Optional.empty();
    }
}
