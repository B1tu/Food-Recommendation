package com.foodrecommendation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.History;
import com.foodrecommendation.service.HistoryService;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService service;

    public HistoryController(HistoryService service) {
        this.service = service;
    }

    // GET /api/history
    @GetMapping
    public List<History> getAll() {
        return service.getAll();
    }

    // GET /api/history/1
    @GetMapping("/{historyId}")
    public ResponseEntity<History> getById(
            @PathVariable Integer historyId) {

        return service.getById(historyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/history/user/1
    @GetMapping("/user/{userId}")
    public List<History> getByUserId(
            @PathVariable Integer userId) {

        return service.getByUserId(userId);
    }

    // POST /api/history
    @PostMapping
    public ResponseEntity<History> create(
            @RequestBody History history) {

        return ResponseEntity.ok(
                service.create(history)
        );
    }

    // DELETE /api/history/1
    @DeleteMapping("/{historyId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer historyId) {

        service.delete(historyId);

        return ResponseEntity.noContent().build();
    }
}