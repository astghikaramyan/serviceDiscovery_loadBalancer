package com.example.gateway_service;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfig {
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("resourceModule", r -> r.path("/resource-api/**")
                        .uri("lb://resource"))

                .route("songModule", r -> r.path("/api/**")
                        .uri("lb://song"))
                .build();
    }
}
