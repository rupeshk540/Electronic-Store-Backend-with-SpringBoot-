package com.electronic.store.services.impl;

import com.electronic.store.dtos.CollectionDto;
import com.electronic.store.dtos.PageableResponse;
import com.electronic.store.entities.Collection;
import com.electronic.store.exceptions.ResourceNotFoundException;
import com.electronic.store.helper.Helper;
import com.electronic.store.repositories.CollectionRepository;
import com.electronic.store.services.CollectionService;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ModelMapper mapper;

    @Value("${collection.coverImage.path}")
    private String imagePath;

    private static final Map<String, String> COLLECTION_ICONS = Map.of(
            "All Deals", "⚡",
            "Hot Deals", "🔥",
            "Trending", "📈",
            "50% Off", "💥",
            "Best Seller", "⭐",
            "New Arrival", "🆕",
            "Best Buy", "💎",
            "Best Rental", "🏆"
    );

    private Logger logger = LoggerFactory.getLogger(CollectionServiceImpl.class);

    @Override
    public CollectionDto create(CollectionDto collectionDto) {
        //creating collectionId: randomly
        String collectionId = UUID.randomUUID().toString();
        collectionDto.setCollectionId(collectionId);
        collectionDto.setAddedDate(new Date());

        // Assign icon based on name
        collectionDto.setIcon(COLLECTION_ICONS.getOrDefault(collectionDto.getTitle(), "📦")); // default icon
        Collection collection = mapper.map(collectionDto, Collection.class);
        Collection savedCollection = collectionRepository.save(collection);
        return mapper.map(savedCollection, CollectionDto.class);
    }

    @Override
    public CollectionDto update(CollectionDto collectionDto, String collectionId) {
        //get category of given id
        Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new ResourceNotFoundException("Collection not found with given id !!"));
        //update category details
        collection.setTitle(collectionDto.getTitle());
        collection.setDescription(collectionDto.getDescription());
        collection.setCoverImage(collectionDto.getCoverImage());
        Collection updatedCollection = collectionRepository.save(collection);
        return mapper.map(updatedCollection, CollectionDto.class);

    }

    @Override
    public void delete(String collectionId) {
        Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new ResourceNotFoundException("Collection not found with given id !!"));

        //delete category coverImage
        String fullPath = imagePath + collection.getCoverImage();
        try{
            Path path = Paths.get(fullPath);
            Files.delete(path);
        }catch (NoSuchFileException ex){
            logger.info("Collection cover image not found in folder");
            ex.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        collectionRepository.delete(collection);

    }

    @Override
    public PageableResponse<CollectionDto> getAll(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
        Pageable pageable = PageRequest.of(pageNumber,pageSize, sort);
        Page<Collection> page = collectionRepository.findAll(pageable);
        //PageableResponse<CollectionDto> pageableResponse = Helper.getPageableResponse(page,CollectionDto.class);
        // Map entity to DTO manually with product count
        List<CollectionDto> dtoList = page.getContent().stream()
                .map(this::mapToDto)
                .toList();

        // Build PageableResponse manually
        PageableResponse<CollectionDto> response = new PageableResponse<>();
        response.setContent(dtoList);
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLastPage(page.isLast());

        return response;
    }

    @Override
    public CollectionDto get(String collectionId) {
        Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new ResourceNotFoundException("Collection not found with given id !!"));
        return mapToDto(collection);
    }


    // ====== HELPER METHOD ======
    private CollectionDto mapToDto(Collection collection) {
        CollectionDto dto = mapper.map(collection, CollectionDto.class); // maps basic fields
        dto.setProductCount(
                collection.getProducts() != null ? collection.getProducts().size() : 0
        ); // manually set productCount
        // Assign icon based on name if null
        if (dto.getIcon() == null) {
            dto.setIcon(COLLECTION_ICONS.getOrDefault(dto.getTitle(), "📦"));
        }
        return dto;
    }
}
