package com.sirashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SiraShopBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiraShopBackendApplication.class, args);
	}

}
