package com.foodrecommendation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodrecommendation.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    Optional<UserProfile> findByUserId(Integer userId);

}