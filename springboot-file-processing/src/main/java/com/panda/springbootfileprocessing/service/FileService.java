package com.panda.springbootfileprocessing.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface FileService {

    ByteArrayOutputStream addTextWatermarkPicture(MultipartFile file);

    ByteArrayOutputStream addTextWatermarkPDDocument(MultipartFile file);

    ByteArrayOutputStream addTextWatermarkOfficeDocuments(MultipartFile file);

    MultipartFile addWatermark(MultipartFile file);
}
