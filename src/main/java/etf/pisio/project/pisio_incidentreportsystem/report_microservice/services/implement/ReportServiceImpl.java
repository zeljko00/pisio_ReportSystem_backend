package etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportImageDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DAO.ReportTypeDAO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportInfoDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.DTO.ReportTypeDTO;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.model.*;
import etf.pisio.project.pisio_incidentreportsystem.report_microservice.services.ReportService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    private final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://localhost:9000")
                    .credentials("V6SgTLZ89DgBGh2YYCDT", "b23RAKpvw1iYtXzjFQm4SBCrY4OoQcgZNlla2gCW")
                    .build();
    private final Gson gson=new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") //
                .create();
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ReportDAO reportDAO;
    private final ReportTypeDAO reportTypeDAO;
    private final ReportImageDAO reportImageDAO;
    private final ModelMapper modelMapper;
    @Value("${reports.images.temp.repo}")
    private String imagesRepo;
    private static final String BUCKET = "images";
    private static final String CONTENT_TYPE = "image/jpeg";

    private final static String subtype_regex = " - ";
    private final static String LAST_DAY = "24h";
    private final static String LAST_WEEK = "7d";
    private final static String LAST_MONTH = "31d";
    private final static String LAST_HALF_YEAR = "6m";
    private final static String GEOCODING_SERVICE_REQUEST_TEMPLATE = "https://api.opencagedata.com/geocode/v1/json?q=LAT+LNG&key=38be49d94e0f42f9af17d878e8c78303";

    private HashMap<String, List<Tuple>> uploadedImages = new HashMap<String, List<Tuple>>();

    public ReportServiceImpl(ReportDAO reportDAO, ReportTypeDAO reportTypeDAO, ReportImageDAO reportImageDAO, ModelMapper modelMapper) {
        this.reportDAO = reportDAO;
        this.reportTypeDAO = reportTypeDAO;
        this.reportImageDAO = reportImageDAO;
        this.modelMapper = modelMapper;
    }

    public boolean approval(long id, boolean approved) {
        Optional<Report> opt = reportDAO.findById(id);
        if (opt.isPresent()) {
            Report report = opt.get();
            System.out.println("Changing report " + id + " state to " + approved);
            report.setApproved(approved);
            reportDAO.saveAndFlush(report);
            return true;
        } else return false;
    }

    public boolean delete(long id) {
        if (reportDAO.findById(id).isPresent()) {
            Report report = reportDAO.findById(id).get();
            report.getImages().stream().forEach(i -> {
                try {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder().bucket(BUCKET).object(i.getId() + ".jpg").build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            reportImageDAO.deleteAllByReportId(id);
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
        if (type != null)
            type += "%";
        if (subtype != null)
            subtype = "%" + subtype;
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
        long tempId = report.getId();

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


                System.out.println("Report tempId: " + tempId);

                report.setId(-1l);
                Report entity = modelMapper.map(report, Report.class);
                Report result = reportDAO.saveAndFlush(entity);
                ReportInfoDTO reportInfoDTO = modelMapper.map(result, ReportInfoDTO.class);
                System.out.println(reportInfoDTO.getDate());

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/report_system/anomaly_detection"))
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(reportInfoDTO)))
                        .header("Content-Type", "application/json")
                        .build();
                try {
                    HttpResponse<String> rep=httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                    System.out.println("Request sent");
                    System.out.println(rep.statusCode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (uploadedImages) {
                    if (uploadedImages.get(Long.toString(tempId)) != null) {
                        System.out.println("Images exist!");
                        List<Tuple> images = uploadedImages.get(Long.toString(tempId));
                        uploadedImages.remove(Long.toString(tempId));
                        int count = 1;
                        for (Tuple t : images) {
                            ReportImage reportImage = new ReportImage();
                            reportImage.setReport(result);
                            reportImageDAO.saveAndFlush(reportImage);
                            String objName = reportImage.getId() + ".jpg";
                            String path = imagesRepo + File.separator + objName;
                            File file = new File(path);
                            try {
                                try {
                                    minioClient.putObject(
                                            PutObjectArgs.builder().bucket(BUCKET).object(objName).stream(
                                                            new ByteArrayInputStream(t.getData()), t.getData().length, -1)
                                                    .contentType(CONTENT_TYPE)
                                                    .build());
                                    System.out.println("Image uploaded successfully!");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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

    public List<ReportTypeDTO> getTypes() {
        return reportTypeDAO.findAll().stream().map(rt -> {
            ReportTypeDTO dto = new ReportTypeDTO();
            dto.setName(rt.getName());
            dto.setSubtypes(rt.getSubtypes().stream().map(st -> st.getName()).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    public boolean saveImage(byte[] data, String id) {
        try {
            String[] tokens = id.split("--");
            String random = tokens[0];
            System.out.println("Image tempId: " + random);
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
            String random = tokens[0];
            ;
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

    public byte[] getImageById(long id) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(id + ".jpg")
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
