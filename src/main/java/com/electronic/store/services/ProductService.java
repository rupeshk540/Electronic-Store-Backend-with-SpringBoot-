package com.electronic.store.services;

import com.electronic.store.dtos.PageableResponse;
import com.electronic.store.dtos.ProductDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public interface ProductService {

    //create
    ProductDto create(ProductDto productDto, MultipartFile[] images) throws IOException;

    //create product with category and collection
    ProductDto createInCategoryAndCollection(ProductDto productDto, String categoryId, MultipartFile[] images) throws IOException;

    //update
    ProductDto update(ProductDto productDto, String productId, MultipartFile[] images) throws IOException;

    //delete
    void delete(String productId);

    //get single
    ProductDto get(String productId);

    //get all
    PageableResponse<ProductDto> getAll(int pageNumber, int pageSize, String sortBy, String sortDir);

    //get all: Live
    PageableResponse<ProductDto> getAllLive(int pageNumber, int pageSize, String sortBy, String sortDir);

    //search product
    PageableResponse<ProductDto> searchByTitle(String subTitle,int pageNumber, int pageSize, String sortBy, String sortDir);

    //create product with category
    ProductDto createWithCategory(ProductDto productDto, String categoryId);

    //update category of product
    ProductDto updateCategory(String productId,String categoryId);

    //get products of a category
    PageableResponse<ProductDto> getAllOfCategory(String categoryId,int pageNumber,int pageSize,String sortBy,String sortDir);

    //create product with collection
    ProductDto createWithCollection(ProductDto productDto, String collectionId);

    //update collection of product
    ProductDto updateCollection(String productId,String colllectionId);

    //get products of a collection
    PageableResponse<ProductDto> getAllOfCollection(String collectionId,int pageNumber,int pageSize,String sortBy,String sortDir);
    //other methods

}
