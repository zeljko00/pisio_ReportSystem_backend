package etf.pisio.project.pisio_incidentreportsystem.auth_microservice.services;

import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model.LoginResponse;

public interface AuthService {
    LoginResponse authenticate(String jwt) throws  Exception;
}
