package etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model;

import lombok.Data;

@Data
public class LoginResponse {
    private String first_name;
    private String last_name;
    private String email;
    private String picture;
    private Role role;
    private String jwt;
}
