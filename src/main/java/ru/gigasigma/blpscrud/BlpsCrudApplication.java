package ru.gigasigma.blpscrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlpsCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlpsCrudApplication.class, args);
    }

}
