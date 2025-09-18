package com.electronic.store.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "collections")
public class Collection {

    @Id
    @Column(name = "id")
    private String collectionId;

    @Column(name = "collection_title", length = 60, nullable = false)
    private String title;

    @Column(name = "collection_desc", length = 500)
    private String description;

    private String coverImage;
    private Date addedDate;

    @ManyToMany(mappedBy = "collections", cascade = {CascadeType.PERSIST, CascadeType.MERGE} ,fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Product> products = new HashSet<>();
}
