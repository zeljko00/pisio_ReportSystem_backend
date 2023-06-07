package etf.pisio.project.pisio_incidentreportsystem.auth_microservice.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebToken;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model.LoginResponse;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model.Role;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.services.AdminService;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.services.AuthService;
import etf.pisio.project.pisio_incidentreportsystem.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String oauth_client_id;
    private static final String EMAIL_DOMAIN_REGEX=".etf.unibl.org";
    private final JwtUtil jwtUtil;
    private final AdminService adminService;

    public AuthServiceImpl(JwtUtil jwtUtil, AdminService adminService) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
    }

    public LoginResponse authenticate(String jwt) throws Exception {
        System.out.println(jwt);
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory()).setAudience(Collections.singletonList(oauth_client_id)).build();
        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), jwt);
        verifier.verify(idToken);
        JsonWebToken.Payload payload = idToken.getPayload();
        String email = (String) payload.get("email");
        System.out.println(email);
        if(!email.endsWith(EMAIL_DOMAIN_REGEX))
            throw  new Exception();
        Role role = Role.CITIZEN;

        LoginResponse response = new LoginResponse();

        if (adminService.getAdminEmails().contains(email)) {

            role = Role.ADMIN;
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String picture = (String) payload.get("picture");

            response.setFirst_name(firstName);
            response.setLast_name(lastName);
            response.setPicture(picture);
        }
        String token = jwtUtil.generateToken(email, role.toString());
        response.setEmail(email);
        response.setJwt(token);
        response.setRole(role);
        return response;
    }
}
