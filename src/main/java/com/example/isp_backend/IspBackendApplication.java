package com.example.isp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class IspBackendApplication {

    public static void main(String[] args) {

        // 🔥 TEMP HASH GENERATION
        System.out.println(
                new BCryptPasswordEncoder().encode("admin123")
        );

        SpringApplication.run(IspBackendApplication.class, args);
    }
}