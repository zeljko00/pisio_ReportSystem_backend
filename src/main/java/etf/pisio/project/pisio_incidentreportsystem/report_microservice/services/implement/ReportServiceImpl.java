package etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportImageDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportTypeDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportTypeDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.*;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.ImageService;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.ReportService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    private final ReportDAO reportDAO;
    private final ReportTypeDAO reportTypeDAO;
    private final ReportImageDAO reportImageDAO;
    private ImageService imageService;
    private final ModelMapper modelMapper;

    private final static String subtype_regex = " - ";
    private final static String LAST_DAY = "24h";
    private final static String LAST_WEEK = "7d";
    private final static String LAST_MONTH = "31d";
    private final static String LAST_HALF_YEAR = "6m";
    private final static String GEOCODING_SERVICE_REQUEST_TEMPLATE = "https://api.opencagedata.com/geocode/v1/json?q=LAT+LNG&key=38be49d94e0f42f9af17d878e8c78303";

    private HashMap<String, List<Tuple>> uploadedImages = new HashMap<String, List<Tuple>>();

    public ReportServiceImpl(ReportDAO reportDAO, ReportTypeDAO reportTypeDAO, ReportImageDAO reportImageDAO, ImageService imageService, ModelMapper modelMapper) {
        this.reportDAO = reportDAO;
        this.reportTypeDAO = reportTypeDAO;
        this.reportImageDAO = reportImageDAO;
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

    public List<ReportDTO> find(Boolean approved, String dateExp, String type, String subtype, String address) {
        System.out.println(approved);
        System.out.println(dateExp);
        System.out.println(type);
        System.out.println(subtype);
        System.out.println(address);

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
        if(type!=null)
            type+="%";
        if(subtype!=null)
            subtype="%"+subtype;
        List<Report> entities = reportDAO.findReportsByApproval(approved, date, type, subtype, address);
        return entities.stream().map(r -> {
            ReportDTO dto = modelMapper.map(r, ReportDTO.class);
            dto.setImagesIDs(r.getImages().stream().map(i -> {
                return i.getId();
            }).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    public Optional<ReportDTO> create(ReportDTO report) {
        long tempId=report.getId();

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


                System.out.println("Report tempId: "+tempId);

                report.setId(-1l);
                Report entity = modelMapper.map(report, Report.class);
                Report result = reportDAO.saveAndFlush(entity);

                synchronized (uploadedImages) {
                    if (uploadedImages.get(Long.toString(tempId)) != null) {
                        System.out.println("Images exist!");
                        List<Tuple> images = uploadedImages.get(Long.toString(tempId));
                        uploadedImages.remove(Long.toString(tempId));
                        int count = 1;
                        for (Tuple t : images) {
                            ReportImage reportImage = new ReportImage();
                            reportImage.setReport(result);
                            System.out.println(t.getId());
                            reportImageDAO.saveAndFlush(reportImage);

                            //prosljedjivanje fotografiej ka min.io

                        }
                    } else
                        System.out.println("no images");
                }

                report.setId(result.getId());
                return Optional.of(report);
            } else
                return Optional.empty();
        } else
            return Optional.empty();
    }
    public List<ReportTypeDTO> getTypes(){
        return reportTypeDAO.findAll().stream().map(rt -> {
            ReportTypeDTO dto=new ReportTypeDTO();
            dto.setName(rt.getName());
            dto.setSubtypes(rt.getSubtypes().stream().map(st -> st.getName()).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    public boolean saveImage(byte[] data, String id) {
        try {
            String[] tokens = id.split("--");
            String random = tokens[0];
            System.out.println("Image tempId: "+random);
            synchronized (uploadedImages) {
                if (!uploadedImages.containsKey(random))
                    uploadedImages.put(random, new ArrayList<Tuple>());
                Tuple temp = new Tuple();
                temp.setId(tokens[1]);
                temp.setData(data);
                uploadedImages.get(random).add(temp);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteImage(String id) {
        try {
            String[] tokens = id.split("--");
            String random = tokens[0];;
            synchronized (uploadedImages) {
                List<Tuple> imgs = uploadedImages.get(random);
                Tuple toDelete = null;
                for (Tuple t : imgs)
                    if (t.getId().equals(tokens[1]))
                        toDelete = t;
                imgs.remove(toDelete);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public byte[] getImageById(long id){
        try{
            //minio
            byte[] result=null;
            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
