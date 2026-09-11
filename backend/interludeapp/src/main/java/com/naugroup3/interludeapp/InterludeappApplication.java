package com.naugroup3.interludeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class InterludeappApplication implements CommandLineRunner {

	public void run(String args[]) throws Exception {
		// Print statement when method is called
		System.out.println("Hello world");
	}

	public static void main(String[] args) {
		SpringApplication.run(InterludeappApplication.class, args);
	}

}
