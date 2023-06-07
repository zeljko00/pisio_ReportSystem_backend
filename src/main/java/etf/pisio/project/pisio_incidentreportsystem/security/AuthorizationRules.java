package etf.pisio.project.pisio_incidentreportsystem.security;

import lombok.Data;

import java.util.List;

@Data
public class AuthorizationRules {
    List<Rule> rules;
}