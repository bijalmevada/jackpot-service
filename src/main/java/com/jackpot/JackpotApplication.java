package com.jackpot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JackpotApplication {
    public static void main(String[] args) {
        SpringApplication.run(JackpotApplication.class, args);
    }
}
