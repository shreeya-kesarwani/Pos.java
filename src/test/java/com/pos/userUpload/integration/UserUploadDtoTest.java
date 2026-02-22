package com.pos.userUpload.integration;

import com.pos.api.UserUploadApi;
import com.pos.dto.UserUploadDto;
import com.pos.pojo.User;
import com.pos.utils.TsvParser;
import com.pos.utils.TsvUploadUtil;
import com.pos.utils.UserTsvUploadUtil;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUploadDtoTest {

    @Mock private UserUploadApi userApi;
    @Mock private Validator validator;

    @InjectMocks private UserUploadDto userUploadDto;

    @Test
    void uploadParsesTsvAndCallsBulkCreateOrUpdate() throws Exception {

        MultipartFile file = new MockMultipartFile(
                "file",
                "u.tsv",
                "text/tab-separated-values",
                ("email\trole\tpassword\n" +
                        "a@b.com\tSUPERVISOR\tpass\n").getBytes(StandardCharsets.UTF_8)
        );

        List<String[]> rows = List.of(
                new String[]{"email", "role", "password"},
                new String[]{"a@b.com", "SUPERVISOR", "pass"}
        );

        User u = new User();
        u.setId(1);

        try (MockedStatic<TsvParser> tsvParser = mockStatic(TsvParser.class);
             MockedStatic<UserTsvUploadUtil> userUtil = mockStatic(UserTsvUploadUtil.class);
             MockedStatic<TsvUploadUtil> uploadUtil = mockStatic(TsvUploadUtil.class)) {

            // read(InputStream)
            tsvParser.when(() -> TsvParser.read(any(InputStream.class)))
                    .thenReturn(rows);

            // validateHeader(String[] actual, String... expected)
            // (optional; you can delete this stub and let real method run)
            tsvParser.when(() -> TsvParser.validateHeader(any(String[].class), any(String[].class)))
                    .thenAnswer(inv -> null);

            // MUST be mutable
            userUtil.when(UserTsvUploadUtil::newSeenEmails)
                    .thenReturn(new HashSet<>());

            uploadUtil.when(() -> TsvUploadUtil.parseOrThrow(anyList(), anyString(), any()))
                    .thenReturn(List.of(u));

            userUploadDto.upload(file);

            verify(userApi).bulkCreateOrUpdate(eq(List.of(u)));
            verifyNoMoreInteractions(userApi);
        }
    }
}