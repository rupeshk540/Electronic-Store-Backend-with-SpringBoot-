package com.electronic.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAddressRequest {


    private String type;
    private String firstName;
    private String lastName;
    private String address;
    private String apartment;
    private String city;
    private String state;
    private String pinCode;
    private boolean isDefault;
}
