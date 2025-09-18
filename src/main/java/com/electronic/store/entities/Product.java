package com.electronic.store.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    private String productId;
    private String title;
    @Column(length = 10000)
    private String description;
    private int price;
    private int discountedPrice;
    private int rentalPrice;
    private int quantity;
    private Date addedDate;
    private boolean live;
    private int stock;
    private int rating;

    @ElementCollection
    @CollectionTable(name = "product_image_urls",joinColumns = @JoinColumn(name = "product_id"))
    private List<String> productImageUrls = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "product_image_public_ids",joinColumns = @JoinColumn(name = "product_id"))
    private List<String> productImagePublicIds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_products")
    @JsonIgnore
    private Category category;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_collection",                     // join table name
            joinColumns = @JoinColumn(name = "product_id"), // FK to Product
            inverseJoinColumns = @JoinColumn(name = "collection_id") // FK to Category
    )
    @JsonIgnore
    private Set<Collection> collections=new HashSet<>();


    // --- Lifecycle hook ---
    @PrePersist
    public void prePersist() {
        if (this.productId == null) {
            this.productId = UUID.randomUUID().toString();
        }
        if (this.addedDate == null) {
            this.addedDate = new Date();
        }
        if (this.collections == null) {
            this.collections = new HashSet<>();
        }
    }

    // --- Add the helper methods here ---
    public void addCollection(Collection collection) {
        this.collections.add(collection);
        collection.getProducts().add(this);
    }

    public void removeCollection(Collection collection) {
        this.collections.remove(collection);
        collection.getProducts().remove(this);
    }


}
