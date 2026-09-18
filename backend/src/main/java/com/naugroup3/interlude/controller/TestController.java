package com.naugroup3.interlude.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.naugroup3.interlude.model.TestEntity;
import com.naugroup3.interlude.repository.TestEntityRepository;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestEntityRepository repository;

    public TestController(TestEntityRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public TestEntity create(@RequestParam String message) {
        TestEntity entity = new TestEntity();
        entity.setMessage(message);
        return repository.save(entity);
    }

    @GetMapping
    public List<TestEntity> getAll() {
        return repository.findAll();
    }
}