package com.example.ftpserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//Singleton
public class MonitoringService {
	private static MonitoringService instance;
    
    private MonitoringService() {
    }

    public static synchronized MonitoringService getInstance() {
        if (instance == null) {
            instance = new MonitoringService();
        }
        return instance;
    }
	
	// Track sessions and their actions
    private final Map<String, List<String>> sessionActivities = new ConcurrentHashMap<>();
    // Track logged users
    private final Set<String> loggedUsers = Collections.synchronizedSet(new HashSet<>());

    // Log user login
    public void userLoggedIn(String username) {
        loggedUsers.add(username);
        System.out.println("User logged in: " + username);
    }

    // Log user logout
    public void userLoggedOut(String username) {
        loggedUsers.remove(username);
        System.out.println("User logged out: " + username);
    }

    // Track session activity
    public void logActivity(String sessionId, String activity) {
        sessionActivities.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(activity);
        System.out.println("Session " + sessionId + ": " + activity);
    }

    // Get active sessions
    public Map<String, List<String>> getSessionActivities() {
        return new HashMap<>(sessionActivities); // Return a copy for safety
    }

    // Get logged users
    public Set<String> getLoggedUsers() {
        return new HashSet<>(loggedUsers); // Return a copy for safety
    }
}
