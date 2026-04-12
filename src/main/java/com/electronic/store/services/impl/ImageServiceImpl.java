package com.electronic.store.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.electronic.store.dtos.ImageDto;
import com.electronic.store.services.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    public ImageServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    @Override
    public ImageDto uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", "products",
                "use_filename", true,
                "unique_filename", true
        );

        // Upload image to Cloudinary
        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

        // Extract URL and publicId
        String url = (String) result.get("secure_url");   // or "url"
        String publicId = (String) result.get("public_id");

        return new ImageDto(url, publicId);
    }

    @Override
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
