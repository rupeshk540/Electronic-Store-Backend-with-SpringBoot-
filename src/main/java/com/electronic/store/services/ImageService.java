package com.electronic.store.services;

import com.electronic.store.dtos.ImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ImageService {

    ImageDto uploadImage(MultipartFile file) throws IOException;
    Map<String, Object> deleteImage(String publicId) throws IOException;
}
