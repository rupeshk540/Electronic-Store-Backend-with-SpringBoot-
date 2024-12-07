package com.electronic.store.dtos;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private String categoryId;

    @NotBlank(message = "Title is required !!")
    @Size(min = 4,message = "Title must be of minimum 4 characters !!")
    private String title;

    @NotBlank(message = "Description required !!")
    private String description;

    private String coverImage;
}
