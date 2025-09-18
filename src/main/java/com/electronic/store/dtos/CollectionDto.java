package com.electronic.store.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollectionDto {

        private String collectionId;

        @NotBlank(message = "Title is required !!")
        @Size(min = 4,message = "Title must be of minimum 4 characters !!")
        private String title;

        @NotBlank(message = "Description required !!")
        private String description;

        private String coverImage;
        private Date addedDate;
        private int productCount;
        private String icon;

}
