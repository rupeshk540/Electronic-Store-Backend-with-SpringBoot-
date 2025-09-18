package com.electronic.store.services;


import com.electronic.store.dtos.CollectionDto;
import com.electronic.store.dtos.PageableResponse;


public interface CollectionService {

    //created
    CollectionDto create(CollectionDto collectionDto);

    //update
    CollectionDto update(CollectionDto collectionDto, String collectionId);

    //delete
    void delete(String collectionId);

    //get all
    PageableResponse<CollectionDto> getAll(int pageNumber, int pageSize, String sortBy, String sortDir);

    //get single category detail
    CollectionDto get(String collectionId);
}
