package com.plutus360.chronologix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ChronologixApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChronologixApplication.class, args);
	}

}
