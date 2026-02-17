package com.pos.integration.dto;

import com.pos.api.UserUploadApi;
import com.pos.dto.UserUploadDto;
import com.pos.pojo.User;
import com.pos.utils.TsvParser;
import com.pos.utils.TsvUploadUtil;
import com.pos.utils.UserTsvUploadUtil;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUploadDtoTest {

    @Mock private UserUploadApi userApi;
    @Mock private Validator validator;

    @InjectMocks private UserUploadDto userUploadDto;

    @Test
    void upload_shouldParseTsvAndCallBulkCreateOrUpdate() throws Exception {
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

            tsvParser.when(() -> TsvParser.read(any())).thenReturn(rows);
            tsvParser.when(() -> TsvParser.validateHeader(any(), any(), any(), any()))
                    .thenAnswer(inv -> null);

            userUtil.when(UserTsvUploadUtil::newSeenEmails).thenReturn(Set.of());

            uploadUtil.when(() -> TsvUploadUtil.parseOrThrow(anyList(), anyString(), any()))
                    .thenReturn(List.of(u));

            userUploadDto.upload(file);

            verify(userApi).bulkCreateOrUpdate(eq(List.of(u)));
        }
    }
}
