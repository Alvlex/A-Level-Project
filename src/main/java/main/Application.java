package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication//signals the starting class
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);//starts the server
	}
}
