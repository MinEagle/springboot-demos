package com.panda.springbootfileprocessing.controller;

import com.panda.springbootfileprocessing.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@RestController
public class FileController {

    @Resource
    FileService fileService;

    @PostMapping("/file")
    public String filesUpload(@RequestPart("file") MultipartFile file) {
        MultipartFile multipartFile = fileService.addWatermark(file);
        try {
            String filename = file.getOriginalFilename();
            String extension = StringUtils.stripFilenameExtension(filename);
            String fileType = StringUtils.getFilenameExtension(filename);
            String fileName = "E:\\images\\" + extension + "." + fileType;
            File file2 = new File(fileName);
            multipartFile.transferTo(file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return multipartFile.getOriginalFilename();
    }

}
