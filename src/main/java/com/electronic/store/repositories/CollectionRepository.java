package com.electronic.store.repositories;

import com.electronic.store.entities.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection,String> {

    long count();
}
