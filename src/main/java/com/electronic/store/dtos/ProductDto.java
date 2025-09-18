package com.electronic.store.dtos;

import com.electronic.store.entities.Category;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductDto {

    private String productId;
    private String title;
    private String description;
    private int price;
    private int discountedPrice;
    private int rentalPrice;
    private int quantity;
    private Date addedDate;
    private boolean live;
    private int stock;
    private int rating;
    private List<String> productImageNames;
    private Category category;
    private Set<CollectionDto> collections;      // multiple collections
    private Set<String> collectionIds;           // IDs for creating/updating

}
