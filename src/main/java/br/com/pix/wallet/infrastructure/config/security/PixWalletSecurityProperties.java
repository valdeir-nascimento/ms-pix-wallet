package br.com.pix.wallet.infrastructure.config.security;

import br.com.pix.wallet.domain.user.UserRole;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "pix.wallet.security")
public class PixWalletSecurityProperties {

    /**
     * Flag that enables or disables the security filter chain.
     */
    private boolean enabled = true;
    private final JwtProperties jwt = new JwtProperties();
    private final BootstrapUser bootstrapUser = new BootstrapUser();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public JwtProperties getJwt() {
        return jwt;
    }

    public BootstrapUser getBootstrapUser() {
        return bootstrapUser;
    }

    public static class JwtProperties {
        private String secret;
        private long expirationMinutes = 60;

        public String getSecret() {
            return secret;
        }

        public void setSecret(final String secret) {
            this.secret = secret;
        }

        public long getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(final long expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }

    public static class BootstrapUser {
        private boolean enabled = false;
        private String username;
        private String password;
        private Set<UserRole> roles = new LinkedHashSet<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }

        public Set<UserRole> getRoles() {
            return roles;
        }

        public void setRoles(final Set<UserRole> roles) {
            this.roles = roles;
        }
    }
}

