package com.electronic.store.controllers;

import com.electronic.store.dtos.*;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.services.CollectionService;
import com.electronic.store.services.FileService;
import com.electronic.store.services.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/collections")
@SecurityRequirement(name = "scheme1")
public class CollectionController {


        @Autowired
        private CollectionService collectionService;
        @Autowired
        private FileService fileService;
        @Autowired
        private ProductService productService;

        @Value("${collection.coverImage.path}")
        private String imageUploadPath;

        private Logger logger = LoggerFactory.getLogger(com.electronic.store.controllers.CollectionController.class);

        //create
        @PostMapping
        public ResponseEntity<CollectionDto> createCollection(@Valid @RequestBody CollectionDto collectionDto){
            //call service to save object
            CollectionDto collectionDto1 = collectionService.create(collectionDto);
            return new ResponseEntity<>(collectionDto1, HttpStatus.CREATED);
        }

        //update
        @PutMapping("{collectionId}")
        public ResponseEntity<CollectionDto> updateCollection(
                @PathVariable String collectionId,
                @RequestBody CollectionDto collectionDto
        ){
            CollectionDto updateCollection = collectionService.update(collectionDto, collectionId);
            return new ResponseEntity<>(updateCollection,HttpStatus.OK);
        }

        //delete collection
        @DeleteMapping("/{collectionId}")
        public ResponseEntity<ApiResponseMessage> deleteCollection(
                @PathVariable String collectionId
        ){
            collectionService.delete(collectionId);
            ApiResponseMessage responseMessage = ApiResponseMessage.builder().message("Collection is deleted successfully !!").status(HttpStatus.OK).success(true).build();
            return new ResponseEntity<>(responseMessage,HttpStatus.OK);
        }

        //get all collections
        @GetMapping
        public ResponseEntity<PageableResponse<CollectionDto>> getAll(
                @RequestParam(value = "pageNumber",defaultValue = "0",required = false) int pageNumber,
                @RequestParam(value = "pageSize",defaultValue = "10",required = false) int pageSize,
                @RequestParam(value = "sortBy",defaultValue = "title",required = false) String sortBy,
                @RequestParam(value = "sortDir",defaultValue = "asc",required = false) String sortDir

        ){
            PageableResponse<CollectionDto> pageableResponse = collectionService.getAll(pageNumber,pageSize,sortBy,sortDir);
            return new ResponseEntity<>(pageableResponse,HttpStatus.OK);
        }

        //get single collection
        @GetMapping("/{collectionId}")
        public ResponseEntity<CollectionDto> getSingle(@PathVariable String collectionId){
            CollectionDto collectionDto = collectionService.get(collectionId);
            return ResponseEntity.ok(collectionDto);
        }

        //upload collection image
        @PostMapping("/image/{collectionId}")
        public ResponseEntity<SingleImageResponse> uploadCoverImage(@RequestParam ("coverImage") MultipartFile image, @PathVariable String collectionId) throws IOException {
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        SingleImageResponse.builder()
                                .message("No image provided")
                                .status(HttpStatus.BAD_REQUEST)
                                .success(false)
                                .build()
                );
            }
            String imageName = fileService.uploadFile(image, imageUploadPath);
            CollectionDto collection = collectionService.get(collectionId);
            collection.setCoverImage(imageName);
            CollectionDto collectionDto = collectionService.update(collection, collectionId);
            SingleImageResponse imageResponse = SingleImageResponse.builder().imageName(imageName).success(true).status(HttpStatus.CREATED).message("Image is uploaded successfully").build();
            return new ResponseEntity<>(imageResponse,HttpStatus.CREATED);
        }

        //serve collection image
        @GetMapping("/image/{collectionId}")
        public void serveUserImage(@PathVariable String collectionId, HttpServletResponse response) throws IOException {
            CollectionDto collection = collectionService.get(collectionId);
            // Check if collection has a cover image
            if (collection.getCoverImage() == null || collection.getCoverImage().isEmpty()) {
                throw new ResourceNotFoundException("Cover image not found for collection: " + collectionId);
            }

            // Load file via FileService
            InputStream resource;
            try {
                resource = fileService.getResource(imageUploadPath, collection.getCoverImage());
            } catch (FileNotFoundException e) {
                throw new ResourceNotFoundException("File not found: " + collection.getCoverImage());
            }
            // Dynamically detect content type
            Path filePath = Paths.get(imageUploadPath, collection.getCoverImage());
            String contentType = Files.probeContentType(filePath);
            response.setContentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);

            logger.info("Serving collection cover image: {}", collection.getCoverImage());
            StreamUtils.copy(resource, response.getOutputStream());
        }

        //create product with collection
        @PostMapping("/{collectionId}/products")
        public ResponseEntity<ProductDto> createProductWithCollection(
                @PathVariable("collectionId") String collectionId,
                @RequestBody ProductDto productDto
        ){
            ProductDto productWithCollection = productService.createWithCollection(productDto,collectionId);
            return new ResponseEntity<>(productWithCollection,HttpStatus.CREATED);
        }

        //update collection of product
        @PutMapping("/{collectionId}/products/{productId}")
        public ResponseEntity<ProductDto> updateCollectionOfProduct(
                @PathVariable String collectionId,
                @PathVariable String productId
        ){
            ProductDto productDto = productService.updateCollection(productId,collectionId);
            return new ResponseEntity<>(productDto,HttpStatus.OK);
        }

        //get products of collections
        @GetMapping("/{collectionId}/products")
        public ResponseEntity<PageableResponse<ProductDto>> getProductsOfCollection(
                @PathVariable String collectionId,
                @RequestParam(value = "pageNumber",defaultValue = "0",required = false) int pageNumber,
                @RequestParam(value = "pageSize",defaultValue = "10",required = false) int pageSize,
                @RequestParam(value = "sortBy",defaultValue = "title",required = false) String sortBy,
                @RequestParam(value = "sortDir",defaultValue = "asc",required = false) String sortDir

        ){
            PageableResponse<ProductDto> response = productService.getAllOfCollection(collectionId,pageNumber,pageSize,sortBy,sortDir);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }



}
