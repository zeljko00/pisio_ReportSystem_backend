package etf.pisio.project.pisio_incidentreportsystem.auth_microservice.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebToken;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model.Jwt;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model.LoginResponse;
import etf.pisio.project.pisio_incidentreportsystem.auth_microservice.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody Jwt access_token) throws Exception {
        LoginResponse response = authService.authenticate(access_token.getValue());
        if (response == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        else
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
