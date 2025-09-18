package com.electronic.store.services.impl;

import com.electronic.store.dtos.PageableResponse;
import com.electronic.store.dtos.ProductDto;
import com.electronic.store.entities.Category;
import com.electronic.store.entities.Collection;
import com.electronic.store.entities.Product;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.helper.Helper;
import com.electronic.store.repositories.CategoryRepository;
import com.electronic.store.repositories.CollectionRepository;
import com.electronic.store.repositories.ProductRepository;
import com.electronic.store.services.FileService;
import com.electronic.store.services.ProductService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private FileService fileService;

    @Value("${product.image.path}")
    private String imagePath;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CollectionRepository collectionRepository;

    private Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public ProductDto create(ProductDto productDto) {
        Product product = mapper.map(productDto, Product.class);
        //set random productId
        String productId = UUID.randomUUID().toString();
        product.setProductId(productId);
        //set added date
        product.setAddedDate(new Date());
        Product saveProduct = productRepository.save(product);
        return mapper.map(saveProduct, ProductDto.class);
    }

    @Override
    public ProductDto createInCategoryAndCollection(ProductDto productDto, String categoryId) {

        Product product = mapper.map(productDto, Product.class);
        //set random productId
//        String productId = UUID.randomUUID().toString();
//        product.setProductId(productId);
        //set added date
//        product.setAddedDate(new Date());
        // Ensure collections is never null after mapping
        if (product.getCollections() == null) {
            product.setCollections(new HashSet<>());
        }


        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setCategory(category);

        //Handle collections
        if (productDto.getCollectionIds() != null && !productDto.getCollectionIds().isEmpty()) {
            List<Collection> collections = collectionRepository.findAllById(productDto.getCollectionIds());
            for (Collection col : collections) {
                product.addCollection(col); // maintains bidirectional sync
            }
        }
        Product saved = productRepository.save(product);
        return mapper.map(saved, ProductDto.class);
    }

    @Override
    public ProductDto update(ProductDto productDto, String productId) {

        //fetch the product of given id
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product not found for given Id !!"));
        // Keep copy of old images before modifying
        List<String> oldImages = new ArrayList<>(product.getProductImageNames());

        product.setTitle(productDto.getTitle());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setDiscountedPrice(productDto.getDiscountedPrice());
        product.setQuantity(productDto.getQuantity());
        product.setLive(productDto.isLive());
        product.setStock(productDto.getStock());
        // Replace images with new list from frontend
        if (productDto.getProductImageNames() != null) {
            List<String> newImages = new ArrayList<>(productDto.getProductImageNames());

            // Find deleted images and remove them physically
            oldImages.stream()
                    .filter(img -> !newImages.contains(img))  // deleted images
                    .forEach(img -> {
                        try {
                            fileService.deleteFile(imagePath, img);
                        } catch (IOException e) {
                            logger.error("Failed to delete image file: {}", img, e);
                        }
                    });


            // Set new images list
            product.setProductImageNames(newImages);
        }
        // Update collections only if provided
        if (productDto.getCollectionIds() != null) {
            product.getCollections().clear();
            if (!productDto.getCollectionIds().isEmpty()) {
                List<Collection> collections = collectionRepository.findAllById(productDto.getCollectionIds());
                for (Collection col : collections) {
                    product.addCollection(col);
                }
            }
        }
        //save the entity
        Product updatedProduct = productRepository.save(product);
        return mapper.map(updatedProduct,ProductDto.class);
    }

    @Override
    public void delete(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product not found for given Id !!"));
        // --- Handle image deletion ---
        if (product.getProductImageNames() != null && !product.getProductImageNames().isEmpty()) {
            for (String imageName : product.getProductImageNames()) {
                String fullPath = imagePath + imageName;
                try {
                    Path path = Paths.get(fullPath);
                    Files.deleteIfExists(path);
                    logger.info("Deleted product image: {}", fullPath);
                } catch (IOException e) {
                    logger.warn("Could not delete image file: {}", fullPath, e);
                }
            }
        }
//        try {
//            Path path = Paths.get(fullPath);
//            Files.delete(path);
//            logger.info("File deleted successfully: " + fullPath);
//        } catch (NoSuchFileException ex) {
//            logger.warn("File not found, unable to delete: " + fullPath, ex);
//        } catch (IOException e) {
//            logger.error("Error deleting file: " + fullPath, e);
//        }
        // --- Handle collections (detach before delete if needed) ---
        if (product.getCollections() != null) {
            for (Collection col : product.getCollections()) {
                col.getProducts().remove(product);  // break bidirectional link
            }
            product.getCollections().clear();
        }

        productRepository.delete(product);

    }

    @Override
    public ProductDto get(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product not found for given Id !!"));
        return mapper.map(product, ProductDto.class);
    }

    @Override
    public PageableResponse<ProductDto> getAll(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> page = productRepository.findAll(pageable);
        return Helper.getPageableResponse(page, ProductDto.class);
    }

    @Override
    public PageableResponse<ProductDto> getAllLive(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> page = productRepository.findByLiveTrue(pageable);
        return Helper.getPageableResponse(page, ProductDto.class);
    }

    @Override
    public PageableResponse<ProductDto> searchByTitle(String subTitle,int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> page = productRepository.findByTitleContaining(subTitle,pageable);
        return Helper.getPageableResponse(page, ProductDto.class);
    }

    @Override
    public ProductDto createWithCategory(ProductDto productDto, String categoryId) {
        //fetch the category from db
      Category category = categoryRepository.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category not found for given id !!"));
        Product product = mapper.map(productDto, Product.class);
        //set random productId
        String productId = UUID.randomUUID().toString();
        product.setProductId(productId);
        //set added date
        product.setAddedDate(new Date());
        product.setCategory(category);
        Product saveProduct = productRepository.save(product);
        return mapper.map(saveProduct, ProductDto.class);

    }

    @Override
    public ProductDto updateCategory(String productId, String categoryId) {
        Product product = productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product not found for given id !!"));
        Category category = categoryRepository.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category not found for given id !!"));
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        return mapper.map(savedProduct,ProductDto.class);
    }

    @Override
    public PageableResponse<ProductDto> getAllOfCategory(String categoryId,int pageNumber,int pageSize,String sortBy,String sortDir) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category not found for given id !!"));
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> page = productRepository.findByCategory(category,pageable);
        return Helper.getPageableResponse(page,ProductDto.class);
    }

    @Override
    public ProductDto createWithCollection(ProductDto productDto, String collectionId) {
        //fetch the collection from db
        Collection collection= collectionRepository.findById(collectionId).orElseThrow(()->new ResourceNotFoundException("Collection not found for given id !!"));
        Product product = mapper.map(productDto, Product.class);
        //set random productId
        String productId = UUID.randomUUID().toString();
        product.setProductId(productId);
        //set added date
        product.setAddedDate(new Date());
        product.addCollection(collection);
        Product saveProduct = productRepository.save(product);
        return mapper.map(saveProduct, ProductDto.class);
    }

    @Override
    public ProductDto updateCollection(String productId, String collectionId) {
        Product product = productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product not found for given id !!"));
        Collection collection = collectionRepository.findById(collectionId).orElseThrow(()->new ResourceNotFoundException("Collection not found for given id !!"));
        product.addCollection(collection);
        Product savedProduct = productRepository.save(product);
        return mapper.map(savedProduct,ProductDto.class);
    }

    @Override
    public PageableResponse<ProductDto> getAllOfCollection(String collectionId, int pageNumber, int pageSize, String sortBy, String sortDir) {
        Collection collection = collectionRepository.findById(collectionId).orElseThrow(()->new ResourceNotFoundException("Collection not found for given id !!"));
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<Product> page = productRepository.findByCollectionsContains(collection,pageable);
        return Helper.getPageableResponse(page,ProductDto.class);
    }
}
