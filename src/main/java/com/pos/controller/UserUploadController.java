package com.pos.controller;

import com.pos.dto.UserUploadDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
public class UserUploadController {

    @Autowired
    private UserUploadDto userBulkDto;

    @RequestMapping(
            value = "/upload",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void upload(@RequestParam("file") MultipartFile file) throws Exception {
        userBulkDto.upload(file);
    }
}
