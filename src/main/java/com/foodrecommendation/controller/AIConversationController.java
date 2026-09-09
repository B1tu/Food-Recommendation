package com.foodrecommendation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.AIConversation;
import com.foodrecommendation.service.AIConversationService;

@RestController
@RequestMapping("/api/ai-conversations")
public class AIConversationController {

    private final AIConversationService aiConversationService;

    public AIConversationController(
            AIConversationService aiConversationService) {
        this.aiConversationService = aiConversationService;
    }

    @GetMapping
    public List<AIConversation> getAllConversations() {
        return aiConversationService.getAllConversations();
    }

    @GetMapping("/{id}")
    public AIConversation getConversationById(
            @PathVariable Integer id) {

        return aiConversationService.getConversationById(id);
    }

    @GetMapping("/user/{userId}")
    public List<AIConversation> getConversationsByUser(
            @PathVariable Integer userId) {

        return aiConversationService
                .getConversationsByUser(userId);
    }

    @PostMapping
    public ResponseEntity<?> createConversation(
            @RequestBody AIConversation conversation) {

        try {
            AIConversation saved = aiConversationService.createConversation(conversation);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public void deleteConversation(
            @PathVariable Integer id) {

        aiConversationService.deleteConversation(id);
    }
}