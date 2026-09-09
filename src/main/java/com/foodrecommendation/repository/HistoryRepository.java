package com.foodrecommendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.foodrecommendation.entity.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {

    List<History> findByUserIdOrderByCreatedAtDesc(Integer userId);
}