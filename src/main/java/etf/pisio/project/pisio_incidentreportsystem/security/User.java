package etf.pisio.project.pisio_incidentreportsystem.security;

import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    private String email;
    private String role;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
    // for more detailed security following methods should have different implementation
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public String getPassword() {
        return email;
    }
}
