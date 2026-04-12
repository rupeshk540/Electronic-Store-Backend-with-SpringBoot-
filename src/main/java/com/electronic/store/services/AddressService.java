package com.electronic.store.services;

import com.electronic.store.dtos.AddressDto;
import com.electronic.store.dtos.CreateAddressRequest;

import java.util.List;

public interface AddressService {

    AddressDto addAddress(String userId,CreateAddressRequest request);
    List<AddressDto> getAddressesOfUser(String userId);
    AddressDto getAddressById(String addressId);
    AddressDto updateAddress(String addressId, CreateAddressRequest request);
    void deleteAddress(String addressId);
}
