package com.playjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.playjava.mapper")
public class PlayjavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlayjavaApplication.class, args);
	}

}
