package com.epam.eco.kafkamanager.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Marat_Mingazov
 */
@Configuration
public class OpenApiConfig {

    private static final String TITLE = "Eco Kafka Manager API";
    private static final String DESCRIPTION = "Service for monitoring and managing Apache Kafka (https://kafka.apache.org/)";
    private static final String VERSION = "0.1.0";
    private static final String LICENSE = "Apache License Version 2.0";
    private static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info().title(TITLE)
                        .description(DESCRIPTION)
                        .version(VERSION)
                        .license(new License().name(LICENSE).url(LICENSE_URL))
                );
    }
}
