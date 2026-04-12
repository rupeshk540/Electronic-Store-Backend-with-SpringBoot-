package com.electronic.store.controllers;

import com.electronic.store.dtos.ApiResponseMessage;
import com.electronic.store.dtos.ImageResponse;
import com.electronic.store.dtos.PageableResponse;
import com.electronic.store.dtos.ProductDto;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.services.CollectionService;
import com.electronic.store.services.FileService;
import com.electronic.store.services.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
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
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
@SecurityRequirement(name = "scheme1")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CollectionService collectionService;

    //create
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestPart ProductDto productDto,@RequestPart(value = "images",required = false) MultipartFile[] images) throws IOException {
        ProductDto createProduct = productService.create(productDto,images);
        return new ResponseEntity<>(createProduct, HttpStatus.CREATED);
    }

    //create in category and collection
    @PostMapping("/categories/{categoryId}/collections")
    public ResponseEntity<ProductDto> createProductInCategoryAndCollection(
            @PathVariable String categoryId,
            @RequestPart("product") ProductDto productDto,
            @RequestPart(value = "images",required = false) MultipartFile[] images
    ) throws IOException {
        ProductDto created = productService.createInCategoryAndCollection(productDto, categoryId,images);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Partial update (stock/live or any field, JSON only)
    @PatchMapping("/{productId}")
    public ResponseEntity<ProductDto> patchProduct(
            @PathVariable String productId,
            @RequestBody ProductDto updates
    ) throws IOException {
        // Call update service with null images → only update fields provided in DTO
        ProductDto updatedProduct = productService.update(updates, productId, null);
        return ResponseEntity.ok(updatedProduct);
    }

    //update
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable String productId,
            @RequestPart("product") ProductDto productDto,
            @RequestPart(value = "images",required = false) MultipartFile[] images
    ) throws IOException {
        ProductDto updatedProduct = productService.update(productDto,productId,images);
        return new ResponseEntity<>(updatedProduct,HttpStatus.OK);
    }

    //delete
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponseMessage> delete(@PathVariable String productId){
        productService.delete(productId);
        ApiResponseMessage responseMessage =  ApiResponseMessage.builder().message("Product deleted successfully !!").status(HttpStatus.OK).success(true).build();
        return new ResponseEntity<>(responseMessage,HttpStatus.OK);
    }

    //get single
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String productId){
        ProductDto productDto = productService.get(productId);
        return new ResponseEntity<>(productDto, HttpStatus.OK);
    }

    //get all
    @GetMapping
    public ResponseEntity<PageableResponse<ProductDto>> getAll(
            @RequestParam(value = "pageNumber",defaultValue = "0",required = false) int pageNumber,
            @RequestParam(value = "pageSize",defaultValue = "10",required = false) int pageSize,
            @RequestParam(value = "sortBy",defaultValue = "title",required = false) String sortBy,
            @RequestParam(value = "sortDir",defaultValue = "asc",required = false) String sortDir

    ){
        PageableResponse<ProductDto> pageableResponse = productService.getAll(pageNumber,pageSize,sortBy,sortDir);
        return new ResponseEntity<>(pageableResponse,HttpStatus.OK);
    }

    //get all live
    @GetMapping("/live")
    public ResponseEntity<PageableResponse<ProductDto>> getAllLive(
            @RequestParam(value = "pageNumber",defaultValue = "0",required = false) int pageNumber,
            @RequestParam(value = "pageSize",defaultValue = "10",required = false) int pageSize,
            @RequestParam(value = "sortBy",defaultValue = "title",required = false) String sortBy,
            @RequestParam(value = "sortDir",defaultValue = "asc",required = false) String sortDir

    ){
        PageableResponse<ProductDto> pageableResponse = productService.getAllLive(pageNumber,pageSize,sortBy,sortDir);
        return new ResponseEntity<>(pageableResponse,HttpStatus.OK);
    }

    //search all
    @GetMapping("/search/{query}")
    public ResponseEntity<PageableResponse<ProductDto>> searchProduct(
            @PathVariable String query,
            @RequestParam(value = "pageNumber",defaultValue = "0",required = false) int pageNumber,
            @RequestParam(value = "pageSize",defaultValue = "10",required = false) int pageSize,
            @RequestParam(value = "sortBy",defaultValue = "title",required = false) String sortBy,
            @RequestParam(value = "sortDir",defaultValue = "asc",required = false) String sortDir

    ){
        PageableResponse<ProductDto> pageableResponse = productService.searchByTitle(query,pageNumber,pageSize,sortBy,sortDir);
        return new ResponseEntity<>(pageableResponse,HttpStatus.OK);
    }

    // Get products of a specific collection
    @GetMapping("/collections/{collectionId}")
    public ResponseEntity<PageableResponse<ProductDto>> getProductsOfCollection(
            @PathVariable String collectionId,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "addedDate", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir
    ) {
        PageableResponse<ProductDto> response = productService.getAllOfCollection(collectionId, pageNumber, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

//    //upload image of product
//    @PostMapping("/image/{productId}")
//    public ResponseEntity<ImageResponse> uploadProductImage(
//            @PathVariable String productId,
//            @RequestParam("productImages") MultipartFile[] images
//    ) throws IOException {
//        //validation: check if no files provided
//        if (images == null || images.length == 0) {
//            return ResponseEntity.badRequest().body(
//                    ImageResponse.builder()
//                            .message("No images provided")
//                            .status(HttpStatus.BAD_REQUEST)
//                            .success(false)
//                            .build()
//            );
//        }
//        List<String> fileNames = new ArrayList<>();
//        for(MultipartFile image : images) {
//            String fileName = fileService.uploadFile(image, imagePath);
//            fileNames.add(fileName);
//        }
//
//        ProductDto productDto = productService.get(productId);
//        if (productDto.getProductImageNames() == null) {
//            productDto.setProductImageNames(new ArrayList<>());
//        }
//        productDto.getProductImageUrls().addAll(fileNames);
//        ProductDto updatedProduct = productService.update(productDto,productId);
//        ImageResponse response = ImageResponse.builder().imageNames(updatedProduct.getProductImageNames()).message("Product image uploaded successfully !!").status(HttpStatus.CREATED).success(true).build();
//        return new ResponseEntity<>(response,HttpStatus.CREATED);
//    }
//
//    //serve specified image of product
//    @GetMapping(value = "/image/{productId}/{fileName}")
//    public void serveProductImage(
//            @PathVariable String productId,
//            @PathVariable String fileName,
//            HttpServletResponse response
//    ) throws IOException {
//        ProductDto productDto = productService.get(productId);
//        // check if the requested file actually belongs to this product
//        if (!productDto.getProductImageUrls().contains(fileName)) {
//            throw new ResourceNotFoundException("Image not found for this product");
//        }
//        InputStream resource  = fileService.getResource(imagePath,fileName);
//        // dynamically detect file type instead of hardcoding JPEG
//        String contentType = Files.probeContentType(Paths.get(imagePath, fileName));
//        response.setContentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
//
//        StreamUtils.copy(resource, response.getOutputStream());
//    }

}
