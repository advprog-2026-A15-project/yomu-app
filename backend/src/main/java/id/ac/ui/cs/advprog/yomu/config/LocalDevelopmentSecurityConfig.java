package id.ac.ui.cs.advprog.yomu.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Konfigurasi keamanan untuk lingkungan pengembangan lokal.
 * Aktif hanya jika property {@code yomu.security.bypass=true} diset.
 * ⚠️ PERINGATAN: Jangan pernah mengaktifkan konfigurasi ini di production!
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
        name = "yomu.security.bypass",
        havingValue = "true"
)
public class LocalDevelopmentSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // Matikan CSRF protection agar Postman/development tools bisa melakukan POST/PUT/DELETE tanpa error 403
                .csrf(AbstractHttpConfigurer::disable)

                // Izinkan semua request masuk tanpa peduli siapa usernya (tanpa token/login)
                // HANYA untuk keperluan development/testing lokal
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}

