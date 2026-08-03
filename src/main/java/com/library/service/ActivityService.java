package com.library.service;

import com.library.model.Activity;
import com.library.repository.ActivityRepository;
import com.library.security.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public void log(String action, String username) {
        Activity activity = new Activity();
        activity.setAction(action);
        activity.setUsername(username);
        activityRepository.save(activity);
    }

    public void log(String action) {
        log(action, SecurityUtil.currentUsername());
    }

    public String username() {
        return SecurityUtil.currentUsername();
    }

    public List<Activity> recent(int limit) {
        return activityRepository.findTop20ByOrderByCreatedAtDesc();
    }
}
