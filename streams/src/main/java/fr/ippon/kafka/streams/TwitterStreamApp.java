package fr.ippon.kafka.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TwitterStreamApp {
    public static void main(String... args) {
        SpringApplication.run(TwitterStreamApp.class, args);
    }
}
