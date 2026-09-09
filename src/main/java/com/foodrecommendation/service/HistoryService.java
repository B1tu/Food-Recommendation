package com.foodrecommendation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foodrecommendation.entity.History;
import com.foodrecommendation.repository.HistoryRepository;

@Service
public class HistoryService {

    private final HistoryRepository repository;

    public HistoryService(HistoryRepository repository) {
        this.repository = repository;
    }

    public List<History> getAll() {
        return repository.findAll();
    }

    public Optional<History> getById(Integer historyId) {
        return repository.findById(historyId);
    }

    public List<History> getByUserId(Integer userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public History create(History history) {

        if (history.getCreatedAt() == null) {
            history.setCreatedAt(LocalDateTime.now());
        }

        return repository.save(history);
    }

    public void delete(Integer historyId) {
        repository.deleteById(historyId);
    }
}