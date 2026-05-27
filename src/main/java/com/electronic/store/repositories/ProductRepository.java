package com.electronic.store.repositories;

import com.electronic.store.entities.Category;
import com.electronic.store.entities.Collection;
import com.electronic.store.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    //search
    Page<Product> findByTitleContaining(String subTitle,Pageable pageable);
    Page<Product> findByLiveTrue(Pageable pageable);
    Page<Product> findByCategory(Category category,Pageable pageable);
    Page<Product>findByCollectionsContains(Collection collection,Pageable pageable);
    long countByStockLessThan(int threshold);

    @Query("""
        SELECT p FROM Product p
        WHERE p.live = true
        AND p.stock > 0
        AND p.discountedPrice <= :price
        AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.averageRating DESC, p.discountedPrice ASC
    """)
    List<Product> searchProductsUnderBudget(
            @Param("keyword") String keyword,
            @Param("price") int price
    );

    @Query("""
        SELECT p FROM Product p
        WHERE p.live = true
        AND p.stock > 0
        AND p.discountedPrice >= :price
        AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.averageRating DESC, p.discountedPrice ASC
    """)
    List<Product> searchProductsAboveBudget(
            @Param("keyword") String keyword,
            @Param("price") int price
    );

    //other methods
    //custom finder methods
    //query methods
}
