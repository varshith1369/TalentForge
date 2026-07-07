package com.talentforge.ui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared storage for user profile data across the application.
 * Allows different components to access and update user profile information.
 */
public class UserProfileCache {

    private static int currentUserId = -1;
    private static int resumeScore = 0;
    private static int problemsSolved = 0;
    private static int aptitudeScore = 0;
    private static int mockInterviews = 0;
    private static int companiesPrepared = 0;
    private static int offersTarget = 1;
    private static BufferedImage profileImage;
    private static final List<ProfileUpdateListener> listeners = new ArrayList<>();

    public interface ProfileUpdateListener {
        void onProfilePictureChanged(BufferedImage image);
    }
    
    public static int getCurrentUserId() { return currentUserId; }
    public static void setCurrentUserId(int id) { currentUserId = id; }
    
    public static int getResumeScore() { return resumeScore; }
    public static void setResumeScore(int score) { resumeScore = score; }

    public static int getProblemsSolved() { return problemsSolved; }
    public static void setProblemsSolved(int count) { problemsSolved = count; }

    public static int getAptitudeScore() { return aptitudeScore; }
    public static void setAptitudeScore(int score) { aptitudeScore = score; }

    public static int getMockInterviews() { return mockInterviews; }
    public static void setMockInterviews(int count) { mockInterviews = count; }

    public static int getCompaniesPrepared() { return companiesPrepared; }
    public static void setCompaniesPrepared(int count) { companiesPrepared = count; }

    public static int getOffersTarget() { return offersTarget; }
    public static void setOffersTarget(int count) { offersTarget = count; }

    public static BufferedImage getProfileImage() {
        return profileImage;
    }

    public static void setProfileImage(BufferedImage image) {
        profileImage = image;
        notifyListeners();
    }

    public static void clearProfileImage() {
        profileImage = null;
    }

    public static void clearAll() {
        currentUserId = -1;
        resumeScore = 0;
        problemsSolved = 0;
        aptitudeScore = 0;
        mockInterviews = 0;
        companiesPrepared = 0;
        offersTarget = 1;
        profileImage = null;
    }

    public static void addProfileUpdateListener(ProfileUpdateListener listener) {
        listeners.add(listener);
    }

    public static void removeProfileUpdateListener(ProfileUpdateListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (ProfileUpdateListener listener : listeners) {
            listener.onProfilePictureChanged(profileImage);
        }
    }
}
