package com.pos.userUpload.integration;

import com.pos.dao.UserDao;
import com.pos.dto.UserUploadDto;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import com.pos.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class UserUploadDtoUploadIT extends AbstractIntegrationTest {

    @Autowired private UserUploadDto userUploadDto;
    @Autowired private UserDao userDao;

    @Test
    void shouldUploadAndCreateUsers_happyFlow() throws Exception {
        var file = new MockMultipartFile(
                "file",
                "users.tsv",
                "text/tab-separated-values",
                ("email\trole\tpassword\n" +
                        "a@b.com\tSUPERVISOR\tpass\n").getBytes(StandardCharsets.UTF_8)
        );

        userUploadDto.upload(file);
        flushAndClear();

        User user = userDao.findByEmail("a@b.com").orElse(null);
        assertNotNull(user);
        assertEquals(UserRole.SUPERVISOR, user.getRole());
        assertNotNull(user.getPasswordHash());
    }
}