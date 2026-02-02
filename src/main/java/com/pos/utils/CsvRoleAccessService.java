package com.pos.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class CsvRoleAccessService {

    private static final String CSV_FILE = "access-control.csv";

    private final List<Rule> rules = new ArrayList<>();

    @PostConstruct
    public void load() {
        try {
            ClassPathResource resource = new ClassPathResource(CSV_FILE);
            if (!resource.exists()) {
                throw new IllegalStateException("Missing " + CSV_FILE + " in src/main/resources");
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                int lineNo = 0;

                while ((line = br.readLine()) != null) {
                    lineNo++;
                    line = line.trim();

                    // Skip empty lines + comments
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    // Expected: METHOD,PATH,ROLES
                    String[] parts = line.split(",", -1);
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("Invalid CSV format at line " + lineNo + ": " + line);
                    }

                    String method = parts[0].trim().toUpperCase(Locale.ROOT);
                    String path = normalizePath(parts[1].trim());
                    String rolesRaw = parts[2].trim();

                    Set<String> roles = parseRoles(rolesRaw);
                    rules.add(new Rule(method, path, roles));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load access rules from " + CSV_FILE + ": " + e.getMessage(), e);
        }
    }

    /**
     * @param method HTTP method (GET/POST/PUT/DELETE...)
     * @param path servletPath (no /api if context-path is /api)
     * @param role user role string (e.g. SUPERVISOR / OPERATOR)
     */
    public boolean isAllowed(String method, String path, String role) {
        if (method == null || path == null || role == null) return false;

        String m = method.trim().toUpperCase(Locale.ROOT);
        String p = normalizePath(path.trim());
        String r = role.trim().toUpperCase(Locale.ROOT);

        // Secure-by-default: deny unless explicitly allowed
        for (Rule rule : rules) {
            if (rule.matches(m, p) && rule.allowsRole(r)) {
                return true;
            }
        }
        return false;
    }

    // ---------------- helpers ----------------

    private static String normalizePath(String p) {
        if (!p.startsWith("/")) p = "/" + p;
        // Remove trailing slash (except root)
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }

    private static Set<String> parseRoles(String rolesRaw) {
        // allow separators: | or ,
        // allow wildcard: *
        if (rolesRaw.equals("*")) {
            return Set.of("*");
        }

        String normalized = rolesRaw.replace("|", ",");
        String[] arr = normalized.split(",", -1);

        Set<String> roles = new HashSet<>();
        for (String s : arr) {
            String v = s.trim();
            if (!v.isEmpty()) roles.add(v.toUpperCase(Locale.ROOT));
        }
        return roles;
    }

    // ---------------- rule model ----------------

    private static final class Rule {
        private final String method;     // e.g. GET / POST / * (optional support)
        private final String pathSpec;   // original path spec from CSV
        private final Pattern pathRegex; // compiled matcher
        private final Set<String> roles; // allowed roles

        Rule(String method, String pathSpec, Set<String> roles) {
            this.method = method;
            this.pathSpec = pathSpec;
            this.roles = roles;

            this.pathRegex = Pattern.compile("^" + toRegex(pathSpec) + "$");
        }

        boolean matches(String reqMethod, String reqPath) {
            // If you want method wildcard in CSV like "*", enable this:
            if (!"*".equals(method) && !method.equals(reqMethod)) return false;
            return pathRegex.matcher(reqPath).matches();
        }

        boolean allowsRole(String role) {
            return roles.contains("*") || roles.contains(role);
        }

        private static String toRegex(String pathSpec) {
            // Supports:
            // 1) /products/*  => prefix wildcard
            // 2) /products/{barcode} => segment wildcard
            // 3) exact paths

            String p = pathSpec;

            // Escape regex special chars first (except * and { } which we handle)
            p = p.replace(".", "\\.")
                    .replace("?", "\\?")
                    .replace("+", "\\+")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace("^", "\\^")
                    .replace("$", "\\$")
                    .replace("|", "\\|");

            // Convert {var} to a single path segment wildcard
            p = p.replaceAll("\\{[^/]+\\}", "[^/]+");

            // Convert trailing /* or any * to match remaining path
            // Example: /products/* => /products/.* (but still matches /products/abc)
            p = p.replace("*", ".*");

            return p;
        }
    }
}
