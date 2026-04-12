package com.electronic.store.dtos;

import com.electronic.store.entities.Category;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDto {

    private String productId;
    private String title;
    private String description;
    private Integer price;
    private Integer discountedPrice;
    private Integer rentalPrice;
//    private Integer quantity;
    private Date addedDate;
    private Boolean live;
    private Integer stock;
    private Integer rating;
    private List<String> productImageUrls;
    private Category category;
    private Set<CollectionDto> collections;      // multiple collections

    private String categoryId;
    private Set<String> collectionIds;           // IDs for creating/updating

}
