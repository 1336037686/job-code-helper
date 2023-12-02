package com.lgx.codehelper.config;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * spring doc配置
 * @author 13360
 * @version 1.0
 * @date 2023-12-02 01:56
 */
@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI restfulOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Spring Boot3 Restful Code Helper API")
                        .description("Code Helper Detail APi")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("SpringDoc Wiki Documentation")
                        .url("https://springdoc.org/v2"));
    }

}

