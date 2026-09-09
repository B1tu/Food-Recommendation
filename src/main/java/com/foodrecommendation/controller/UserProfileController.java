package com.foodrecommendation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodrecommendation.entity.UserProfile;
import com.foodrecommendation.service.UserProfileService;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    // GET /api/profiles
    @GetMapping
    public List<UserProfile> getAllProfiles() {
        return userProfileService.getAllProfiles();
    }

    // GET /api/profiles/user/1
    @GetMapping("/user/{userId}")
    public UserProfile getProfileByUserId(
            @PathVariable Integer userId) {

        return userProfileService.getProfileByUserId(userId);
    }

    // POST /api/profiles
    @PostMapping
    public UserProfile createProfile(
            @RequestBody UserProfile profile) {

        return userProfileService.createProfile(profile);
    }

    // PUT /api/profiles/1
    @PutMapping("/{profileId}")
    public UserProfile updateProfile(
            @PathVariable Integer profileId,
            @RequestBody UserProfile profile) {

        return userProfileService.updateProfile(
                profileId,
                profile
        );
    }

    // DELETE /api/profiles/1
    @DeleteMapping("/{profileId}")
    public String deleteProfile(
            @PathVariable Integer profileId) {

        userProfileService.deleteProfile(profileId);

        return "Xóa profile thành công";
    }
}