package com.electronic.store.dtos;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleImageResponse {

        private String imageName;
        private String message;
        private boolean success;
        private HttpStatus status;
}
