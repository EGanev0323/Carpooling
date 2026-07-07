package bg.tu_sofia.carpooling;

import bg.tu_sofia.carpooling.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableCaching
@EnableMethodSecurity
@EnableConfigurationProperties(AppProperties.class)
public class CarpoolingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarpoolingApplication.class, args);
    }
}
