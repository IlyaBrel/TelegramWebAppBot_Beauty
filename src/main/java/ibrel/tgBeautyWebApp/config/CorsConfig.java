package ibrel.tgBeautyWebApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * Список разрешённых origin через запятую.
     * Задаётся в application.properties:
     *   cors.allowed-origins=https://xxxx-xx-xx-xx-xx.ngrok-free.app,http://localhost:5174
     */
    @Value("${cors.allowed-origins}")
    private String allowedOriginsRaw;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Разбиваем строку по запятой, убираем пробелы
        List<String> origins = List.of(allowedOriginsRaw.split(","))
                .stream()
                .map(String::trim)
                .toList();

        origins.forEach(config::addAllowedOrigin);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}