package fr.ippon.kafka.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class TwitterStreamApp {
    public static void main(String... args) {
        SpringApplication.run(TwitterStreamApp.class, args);
    }
}
