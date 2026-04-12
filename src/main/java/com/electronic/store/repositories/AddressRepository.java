package com.electronic.store.repositories;

import com.electronic.store.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address,String> {

    List<Address> findByUser_UserId(String userId);
}
