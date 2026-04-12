package com.electronic.store.dtos;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AddressDto {

    private String id;
    private String userId;
    private String type;       // Home, Work, etc.
    private String firstName;
    private String lastName;
    private String address;
    private String apartment;
    private String city;
    private String state;
    private String pinCode;
    private boolean isDefault;
}
