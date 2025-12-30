package acs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;



@SpringBootApplication
@EnableAsync
public class AccessControlApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccessControlApplication.class, args);
    }
}