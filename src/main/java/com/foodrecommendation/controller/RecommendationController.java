package com.foodrecommendation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.Recommendation;
import com.foodrecommendation.service.RecommendationService;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(
            RecommendationService service) {

        this.service = service;
    }

    // ==========================================
    // GET ALL
    // GET /api/recommendations
    // ==========================================

    @GetMapping
    public List<Recommendation> getAll() {

        return service.getAll();
    }

    // ==========================================
    // GET BY ID
    // GET /api/recommendations/1
    // ==========================================

    @GetMapping("/{recommendationId}")
    public ResponseEntity<Recommendation> getById(
            @PathVariable Integer recommendationId) {

        return service.getById(recommendationId)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    // ==========================================
    // GET BY USER
    // GET /api/recommendations/user/1
    // ==========================================

    @GetMapping("/user/{userId}")
    public List<Recommendation> getByUserId(
            @PathVariable Integer userId) {

        return service.getByUserId(userId);
    }

    // ==========================================
    // CREATE
    // POST /api/recommendations
    // ==========================================

    @PostMapping
    public ResponseEntity<Recommendation> create(
            @RequestBody Recommendation recommendation) {

        return ResponseEntity.ok(
                service.create(recommendation)
        );
    }

    // ==========================================
    // UPDATE
    // PUT /api/recommendations/1
    // ==========================================

    @PutMapping("/{recommendationId}")
    public ResponseEntity<Recommendation> update(
            @PathVariable Integer recommendationId,
            @RequestBody Recommendation recommendation) {

        return ResponseEntity.ok(
                service.update(
                        recommendationId,
                        recommendation
                )
        );
    }

    // ==========================================
    // DELETE
    // DELETE /api/recommendations/1
    // ==========================================

    @DeleteMapping("/{recommendationId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer recommendationId) {

        service.delete(recommendationId);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // REFRESH
    // POST /api/recommendations/refresh/1
    // ==========================================

    @PostMapping("/refresh/{userId}")
    public ResponseEntity<List<Recommendation>> refresh(
        @PathVariable Integer userId) {

        return ResponseEntity.ok(
                service.refreshRecommendations(userId)
        );
    }
}