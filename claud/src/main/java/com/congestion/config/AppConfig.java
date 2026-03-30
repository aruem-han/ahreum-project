
package com.congestion.config; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
            .allowedOrigins("http://localhost:5173") 
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메소드
            .allowedHeaders("*") // 모든 헤더 허용
            .allowCredentials(true); // 쿠키/인증 정보 허용
    }
}
