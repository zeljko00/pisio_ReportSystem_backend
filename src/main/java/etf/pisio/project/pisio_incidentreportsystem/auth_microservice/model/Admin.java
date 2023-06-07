package etf.pisio.project.pisio_incidentreportsystem.auth_microservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Admin {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
}
