package com.electronic.store.controllers;

import com.electronic.store.dtos.AddressDto;
import com.electronic.store.dtos.CreateAddressRequest;
import com.electronic.store.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/{userId}")
    public ResponseEntity<AddressDto> addAddress(@PathVariable String userId,@RequestBody CreateAddressRequest request) {
        return new ResponseEntity<>(addressService.addAddress(userId, request), HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<AddressDto>> getAddressesOfUser(@PathVariable String userId) {
        return ResponseEntity.ok(addressService.getAddressesOfUser(userId));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDto> getAddress(@PathVariable String addressId) {
        return ResponseEntity.ok(addressService.getAddressById(addressId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable String addressId, @RequestBody CreateAddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable String addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

}
