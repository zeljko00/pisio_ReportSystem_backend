package etf.pisio.project.pisio_incidentreportsystem.security;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
    public static final long JWT_VALIDITY = 5 * 60 * 60;
    @Value("${jwt.secretKey}")
    private String secretKey;

    public String getEmailFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }
    public String getRoleFromToken(String token){return getAllClaimsFromToken(token).get("role",String.class);}

    public Date getExpirationDateFromToken(String token) {       //retrieve expiration date from jwt
        return getAllClaimsFromToken(token).getExpiration();
    }
    private Claims getAllClaimsFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            return claims;
        } catch (Exception e) {
            System.out.println("Error while getting claims from jwt - jwt probably changed or signed with different key!");
            e.printStackTrace();
            throw e;
        }
    }
    private Boolean isTokenExpired(String token) {       //check if the token has expired
        boolean expired=getExpirationDateFromToken(token).before(new Date());
        if(expired)
            System.out.println("Token expired!");
        return  expired;
    }

    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",role);
        return Jwts.builder().setClaims(claims).setSubject(email).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
