package com.extech.IntegracionesApis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IntegracionesApisApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntegracionesApisApplication.class, args);
	}

}
