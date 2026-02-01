package com.pos.security;

import com.pos.model.constants.UserRole;

import java.util.Set;

public record AccessRule(String pathPattern, String httpMethod, Set<UserRole> allowedRoles) {}
