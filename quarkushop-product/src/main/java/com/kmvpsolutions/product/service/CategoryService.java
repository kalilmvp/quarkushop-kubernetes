package com.kmvpsolutions.product.service;

import com.kmvpsolutions.commons.dto.CategoryDTO;
import com.kmvpsolutions.commons.dto.ProductDTO;
import com.kmvpsolutions.product.dao.CategoryRepository;
import com.kmvpsolutions.product.dao.ProductRepository;
import com.kmvpsolutions.product.domain.Category;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    ProductRepository productRepository;

    public List<CategoryDTO> findAll() {
        log.debug("Request to get all Categories");

        return this.categoryRepository.findAll()
                .stream()
                .map(category -> mapToDTO(category,
                        productRepository.countAllByCategoryId(category.getId())))
                .collect(Collectors.toList());
    }

    public CategoryDTO findById(Long id) {
        log.debug("Request to get Category: {}", id);

        return this.categoryRepository.findById(id)
                .map(category -> mapToDTO(category,
                        productRepository.countAllByCategoryId(category.getId())))
                .orElse(null);
    }

    public CategoryDTO create(CategoryDTO categoryDTO) {
        log.debug("Request to create Category: {}", categoryDTO);

        return mapToDTO(this.categoryRepository.save(
                new Category(
                        categoryDTO.getName(),
                        categoryDTO.getDescription()
                )
        ), 0L);
    }

    public void delete(Long id) {
        log.debug("Request to delete Category: {}", id);
        log.debug("Deleting all products for the category: {}", id);

        // if there is any product with the category being deleted, does not allow it
        if (this.productRepository.findAllByCategoryId(id).stream().count() > 0) {
            throw new IllegalStateException("There is a product with this category associated");
        }

        this.productRepository.deleteAllByCategoryId(id);

        log.debug("Deleting Category: {}", id);
        this.categoryRepository.deleteById(id);
    }

    public List<ProductDTO> findProductsByCategoryId(Long id) {
        return this.productRepository.findAllByCategoryId(id)
                .stream()
                .map(ProductService::mapToDTO)
                .collect(Collectors.toList());
    }

    public static CategoryDTO mapToDTO(Category category, Long productsCount) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                productsCount
        );
    }
}
