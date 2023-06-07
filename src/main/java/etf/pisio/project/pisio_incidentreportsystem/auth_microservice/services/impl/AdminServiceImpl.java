package etf.pisio.project.pisio_incidentreportsystem.auth_microservice.services.impl;

import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.DAO.AdminDAO;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.services.AdminService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
public class AdminServiceImpl implements AdminService {
    private final AdminDAO adminDAO;

    public AdminServiceImpl(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    public List<String> getAdminEmails(){
        return adminDAO.findAll().stream().map(a -> a.getEmail()).collect(Collectors.toList());
    }
}
