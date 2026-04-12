package com.electronic.store.services.impl;

import com.electronic.store.dtos.AddressDto;
import com.electronic.store.dtos.CreateAddressRequest;
import com.electronic.store.entities.Address;
import com.electronic.store.entities.User;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.repositories.AddressRepository;
import com.electronic.store.repositories.UserRepository;
import com.electronic.store.services.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private  AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressDto addAddress(String userId,CreateAddressRequest request) {
        Address address = modelMapper.map(request, Address.class);
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User not found for given Id !"));
        address.setUser(user);
        Address saved = addressRepository.save(address);
        return modelMapper.map(saved, AddressDto.class);
    }

    @Override
    public AddressDto updateAddress(String addressId, CreateAddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found !!"));
        modelMapper.map(request, address); // updates existing entity
        Address updated = addressRepository.save(address);
        return modelMapper.map(updated, AddressDto.class);
    }

    @Override
    public List<AddressDto> getAddressesOfUser(String userId) {
        List<Address> addresses = addressRepository.findByUser_UserId(userId);
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDto getAddressById(String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found !!"));
        return modelMapper.map(address, AddressDto.class);
    }


    @Override
    public void deleteAddress(String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found !!"));
        addressRepository.delete(address);
    }
}

