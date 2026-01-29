//package com.pos.security;
//
//import com.pos.pojo.UserRole;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RoleResolver {
//
//    @Autowired
//    private UserRoleMappingDao mappingDao;
//
//    public UserRole resolveRole(String email) {
//        if (email == null) return UserRole.OPERATOR;
//
//        String key = email.trim().toLowerCase();
//        return mappingDao.findByEmail(key)
//                .map(m -> m.getRole())
//                .orElse(UserRole.OPERATOR);
//    }
//}
