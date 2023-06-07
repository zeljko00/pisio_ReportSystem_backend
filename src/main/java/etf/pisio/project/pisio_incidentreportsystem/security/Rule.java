package etf.pisio.project.pisio_incidentreportsystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    private List<String> methods;
    private String pattern;
    private List<String> roles;
}