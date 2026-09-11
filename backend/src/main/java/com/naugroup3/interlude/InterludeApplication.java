package com.naugroup3.interlude;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class InterludeApplication implements CommandLineRunner {

	public void run(String args[]) throws Exception {
		System.out.println("Hello world");
	}

	public static void main(String[] args) {
		SpringApplication.run(InterludeApplication.class, args);
	}

}
