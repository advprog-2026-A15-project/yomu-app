package id.ac.ui.cs.advprog.yomu.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Konfigurasi keamanan untuk lingkungan pengembangan lokal.
 *
 * Konfigurasi ini HANYA diaktifkan ketika profil "local" atau "dev" aktif.
 * - Menonaktifkan CSRF protection agar tools seperti Postman dapat melakukan POST/PUT/DELETE tanpa error 403
 * - Mengizinkan semua request masuk tanpa memerlukan autentikasi
 *
 * ⚠️ PERINGATAN: Jangan pernah menggunakan konfigurasi ini di production!
 *
 * @author Development Team
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@Profile({"local", "dev"})
public class LocalDevelopmentSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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

