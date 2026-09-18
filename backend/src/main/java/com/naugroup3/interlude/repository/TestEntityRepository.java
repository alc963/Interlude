package com.naugroup3.interlude.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.model.TestEntity;

public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
}