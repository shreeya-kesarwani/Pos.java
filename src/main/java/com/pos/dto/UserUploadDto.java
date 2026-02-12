package com.pos.dto;

import com.pos.api.UserUploadApi;
import com.pos.exception.ApiException;
import com.pos.pojo.User;
import com.pos.utils.TsvParser;
import com.pos.utils.TsvUploadUtil;
import com.pos.utils.UserTsvUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class UserUploadDto extends AbstractDto {

    @Autowired
    private UserUploadApi userApi;

    public void upload(MultipartFile file) throws ApiException, IOException {

        List<String[]> rows = TsvParser.read(file.getInputStream());
        TsvParser.validateHeader(rows.get(0), "email", "role", "password");

        Set<String> seenEmails = UserTsvUploadUtil.newSeenEmails();

        List<User> users = TsvUploadUtil.parseOrThrow(
                rows,
                "user_upload_errors",
                (r, lineNumber) -> UserTsvUploadUtil.parseRowOrThrow(r, lineNumber, seenEmails)
        );

        userApi.bulkCreateOrUpdate(users);
    }
}
