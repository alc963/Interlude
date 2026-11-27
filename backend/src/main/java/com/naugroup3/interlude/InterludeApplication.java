package com.naugroup3.interlude;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naugroup3.interlude.services.ITunes.ITunes;

import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@RestController
public class InterludeApplication implements CommandLineRunner {

	private final ITunes itunes;
	private boolean is_ready = false;

    public InterludeApplication(ITunes itunes) {
        this.itunes = itunes;
    }

	@GetMapping("/")
	public String ping() {
		return "Interlude server is online";
	}

	@GetMapping("/health")
	public boolean health() {
		return is_ready;
	}

	public void run(String args[]) throws Exception {
		itunes.enqueue_all_unprocessed_tracks();
		System.out.println("Ready...");
		is_ready = true;
	}

	public static void main(String[] args) {
		SpringApplication.run(InterludeApplication.class, args);
	}
}
