package com.electronic.store.repositories;

import com.electronic.store.entities.Category;
import com.electronic.store.entities.Collection;
import com.electronic.store.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    //search
    Page<Product> findByTitleContaining(String subTitle,Pageable pageable);
    Page<Product> findByLiveTrue(Pageable pageable);
    Page<Product> findByCategory(Category category,Pageable pageable);
    Page<Product>findByCollectionsContains(Collection collection,Pageable pageable);
    long countByStockLessThan(int threshold);


    //other methods
    //custom finder methods
    //query methods
}
