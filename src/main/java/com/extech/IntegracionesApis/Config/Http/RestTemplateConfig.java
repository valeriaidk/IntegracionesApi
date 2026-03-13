package com.extech.IntegracionesApis.Config.Http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    private static final String INFOBIP_API_KEY = "d43a627ef1addc6d51b634123ae69574-b8bfc8dc-2ec0-47c9-8b59-6e5e152744fe";
    private static final String INFOBIP_BASE_URL = "https://8vgly1.api.infobip.com";
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Configurar headers para Infobip
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (request.getURI().toString().contains("infobip.com")) {
                request.getHeaders().add("Authorization", "App " + INFOBIP_API_KEY);
                request.getHeaders().add("Content-Type", "application/json");
                request.getHeaders().add("Accept", "application/json");
            }
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }
    
    @Bean
    public String infobipApiKey() {
        return INFOBIP_API_KEY;
    }
    
    @Bean
    public String infobipBaseUrl() {
        return INFOBIP_BASE_URL;
    }
}
