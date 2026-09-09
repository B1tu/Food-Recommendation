package com.foodrecommendation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.foodrecommendation.entity.UserProfile;
import com.foodrecommendation.repository.UserProfileRepository;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    // Lấy tất cả profile
    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll();
    }

    // Lấy profile theo user_id
    public UserProfile getProfileByUserId(Integer userId) {
        return userProfileRepository
                .findByUserId(userId)
                .orElse(null);
    }

    // Tạo profile
    public UserProfile createProfile(UserProfile profile) {
        return userProfileRepository.save(profile);
    }

    // Cập nhật profile
    public UserProfile updateProfile(Integer profileId, UserProfile profile) {

        UserProfile existing = userProfileRepository
                .findById(profileId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy profile với ID: " + profileId)
                );

        existing.setUserId(profile.getUserId());
        existing.setAge(profile.getAge());
        existing.setPreferences(profile.getPreferences());
        existing.setDietaryPreferences(profile.getDietaryPreferences());
        existing.setBudget(profile.getBudget());

        return userProfileRepository.save(existing);
    }

    // Xóa profile
    public void deleteProfile(Integer profileId) {

        if (!userProfileRepository.existsById(profileId)) {
            throw new RuntimeException(
                    "Không tìm thấy profile với ID: " + profileId
            );
        }

        userProfileRepository.deleteById(profileId);
    }
}