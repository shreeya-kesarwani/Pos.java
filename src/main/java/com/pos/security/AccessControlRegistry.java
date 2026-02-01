package com.pos.security;

import com.pos.model.constants.UserRole;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class AccessControlRegistry {

    private final List<AccessRule> rules = new ArrayList<>();

    @PostConstruct
    public void load() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource("access-control.csv").getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // path,method,roles
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                String path = parts[0].trim();
                String method = parts[1].trim().toUpperCase(Locale.ROOT);
                String rolesCsv = parts[2].trim();

                Set<UserRole> roles = new HashSet<>();
                for (String r : rolesCsv.split("\\s*\\|\\s*|\\s*;\\s*|\\s*\\s+\\s*|\\s*,\\s*")) {
                    // allow comma-separated OR space-separated roles; robust parsing
                    if (r == null || r.isBlank()) continue;
                    roles.add(UserRole.valueOf(r.trim().toUpperCase(Locale.ROOT)));
                }

                if (!roles.isEmpty()) {
                    rules.add(new AccessRule(path, method, roles));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load access-control.csv", e);
        }
    }

    public List<AccessRule> getRules() {
        return Collections.unmodifiableList(rules);
    }
}
