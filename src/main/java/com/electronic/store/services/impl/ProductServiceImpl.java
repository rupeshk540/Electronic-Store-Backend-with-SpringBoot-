package com.electronic.store.services.impl;

import com.electronic.store.dtos.ImageDto;
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
import com.electronic.store.services.ImageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ImageService imageService;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CollectionRepository collectionRepository;

    private Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public ProductDto create(ProductDto productDto, MultipartFile[] images) throws IOException {
        Product product = mapper.map(productDto, Product.class);
        //set random productId
        String productId = UUID.randomUUID().toString();
        product.setProductId(productId);
        //set added date
        product.setAddedDate(new Date());

        // --- Handle image upload ---
        if (images != null && images.length > 0) {
            for (MultipartFile file : images) {
                ImageDto uploadResult = imageService.uploadImage(file);
                product.getProductImageUrls().add(uploadResult.getUrl());
                product.getProductImagePublicIds().add(uploadResult.getPublicId());
            }
        }
        Product saveProduct = productRepository.save(product);
        return mapper.map(saveProduct, ProductDto.class);
    }

//    @Override
//    public ProductDto createInCategoryAndCollection(ProductDto productDto, String categoryId, MultipartFile[] images) throws IOException {
//        Product product = mapper.map(productDto, Product.class);
//        // Set random productId and added date if needed
//
//        // --- Set category ---
//        Category category = categoryRepository.findById(categoryId)
//                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
//        product.setCategory(category);
//
//        // --- Handle collections ---
//        if (productDto.getCollectionIds() != null && !productDto.getCollectionIds().isEmpty()) {
//            List<Collection> collections = collectionRepository.findAllById(productDto.getCollectionIds());
//            for (Collection col : collections) {
//                product.addCollection(col); // maintains bidirectional sync
//            }
//        }
//        // --- Handle image upload ---
//        if (images != null && images.length > 0) {
//            for (MultipartFile file : images) {
//                Map<String, Object> uploadResult = imageService.uploadImage(file);
//                product.getProductImageUrls().add(uploadResult.get("secure_url").toString());
//                product.getProductImagePublicIds().add(uploadResult.get("public_id").toString());
//            }
//        }
//        Product saved = productRepository.save(product);
//        return mapper.map(saved, ProductDto.class);
//    }

    @Override
    public ProductDto createInCategoryAndCollection(ProductDto productDto, String categoryId, MultipartFile[] images) throws IOException {

        // --- Map DTO → Entity (basic fields only) ---
        Product product = mapper.map(productDto, Product.class);

        // --- Category ---
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategory(category);

        // --- Collections ---
        if (productDto.getCollectionIds() != null && !productDto.getCollectionIds().isEmpty()) {
            Set<Collection> collections = new HashSet<>(collectionRepository.findAllById(productDto.getCollectionIds()));
            product.setCollections(collections);
        }

        // --- Images ---
        if (images != null && images.length > 0) {
            List<String> urls = new ArrayList<>();
            List<String> publicIds = new ArrayList<>();

            for (MultipartFile file : images) {
                ImageDto result = imageService.uploadImage(file);
                urls.add(result.getUrl());
                publicIds.add(result.getPublicId());
            }

            product.setProductImageUrls(urls);
            product.setProductImagePublicIds(publicIds);
        }

        // --- Save product ---
        Product saved = productRepository.save(product);

        // --- Map Entity → DTO for response ---
        ProductDto response = mapper.map(saved, ProductDto.class);

        // Set IDs explicitly (since we don’t want full category/collections in DTO)
        response.setCategoryId(saved.getCategory().getCategoryId());
        response.setCollectionIds(saved.getCollections() != null
                ? saved.getCollections().stream().map(Collection::getCollectionId).collect(Collectors.toSet())
                : Collections.emptySet());

        return response;
    }



    @Override
    public ProductDto update(ProductDto productDto, String productId,MultipartFile[] images) throws IOException {

        //fetch the product of given id
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product not found for given Id !!"));

        // update fields only if not null
        if (productDto.getTitle() != null) product.setTitle(productDto.getTitle());
        if (productDto.getDescription() != null) product.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) product.setPrice(productDto.getPrice());
        if (productDto.getDiscountedPrice() != null) product.setDiscountedPrice(productDto.getDiscountedPrice());
        if (productDto.getLive() != null) product.setLive(productDto.getLive());
        if (productDto.getStock() != null) product.setStock(productDto.getStock());

        // --- handle images ---
        if ((images != null && images.length > 0) || productDto.getProductImageUrls() != null) {

            // Step 1: Delete removed images (but at least one must remain, frontend ensures this)
            if (productDto.getProductImageUrls() != null) {
                List<String> urlsToKeep = productDto.getProductImageUrls();

                // Map old URLs -> publicIds
                Map<String, String> oldUrlToPublicId = new HashMap<>();
                for (int i = 0; i < product.getProductImageUrls().size(); i++) {
                    oldUrlToPublicId.put(product.getProductImageUrls().get(i),
                            product.getProductImagePublicIds().get(i));
                }

                // Delete images not in urlsToKeep
                for (String oldUrl : oldUrlToPublicId.keySet()) {
                    if (!urlsToKeep.contains(oldUrl)) {
                        imageService.deleteImage(oldUrlToPublicId.get(oldUrl));
                        product.getProductImageUrls().remove(oldUrl);
                        product.getProductImagePublicIds().remove(oldUrlToPublicId.get(oldUrl));
                    }
                }
            }

            // Step 2: Upload new images
            if (images != null && images.length > 0) {
                for (MultipartFile file : images) {
                    ImageDto uploadResult = imageService.uploadImage(file);
                    product.getProductImageUrls().add(uploadResult.getUrl());
                    product.getProductImagePublicIds().add(uploadResult.getPublicId());
                }
            }

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
        if (product.getProductImagePublicIds() != null) {
            for (String publicId : product.getProductImagePublicIds()) {
                try {
                    imageService.deleteImage(publicId);
                } catch (IOException e) {
                    logger.warn("Failed to delete Cloudinary image: {}", publicId, e);
                }
            }
        }
        // Handle collections (detach before delete if needed) ---
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
