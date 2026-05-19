package com.electronic.store.config;

import com.electronic.store.dtos.ReviewDto;
import com.electronic.store.entities.Review;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setAmbiguityIgnored(true);

        return modelMapper;
    }
}
