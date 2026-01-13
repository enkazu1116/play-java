package com.playjava.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("在庫管理システム API")
                        .version("1.0.0")
                        .description("在庫管理システムのREST API仕様書"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("開発環境")
                ));
    }
}
