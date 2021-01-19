package com.kmvpsolutions.product.service;

import com.kmvpsolutions.commons.dto.ProductDTO;
import com.kmvpsolutions.product.dao.CategoryRepository;
import com.kmvpsolutions.product.dao.ProductRepository;
import com.kmvpsolutions.product.domain.Product;
import com.kmvpsolutions.product.domain.enums.ProductStatus;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
public class ProductService {

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    ProductRepository productRepository;

    public List<ProductDTO> findAll() {
        log.debug("Request to get all products");

        return this.productRepository.findAll()
                .stream()
                .map(ProductService::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO findById(Long id) {
        log.debug("Request to get Product: {}", id);

        return this.productRepository.findById(id)
                .map(ProductService::mapToDTO)
                .orElse(null);
    }

    public Long countAll() {
        log.debug("Request to count all products");
        return this.productRepository.count();
    }

    public Long countByCategoryId(Long categoryId) {
        log.debug("Request to count all products by categoryId: {}", categoryId);
        return this.productRepository.countAllByCategoryId(categoryId);
    }

    public ProductDTO create(ProductDTO productDTO) {
        log.debug("Request to create Product {}", productDTO);

        return mapToDTO(this.productRepository.save(
                new Product(
                        productDTO.getName(),
                        productDTO.getDescription(),
                        productDTO.getPrice(),
                        ProductStatus.valueOf(productDTO.getStatus()),
                        productDTO.getSalesCounter(),
                        Collections.emptySet(),
                        this.categoryRepository.findById(productDTO.getCategoryId()).orElse(null)
                )
        ));
    }

    public void delete(Long id) {
        log.debug("Request to delete Product: {}", id);

        this.productRepository.deleteById(id);
    }

    public List<ProductDTO> findByCategoryId(Long categoryId) {
        log.debug("Request to get Product by categoryId: {}", categoryId);

        return this.productRepository.findAllByCategoryId(categoryId)
                .stream()
                .map(ProductService::mapToDTO)
                .collect(Collectors.toList());
    }

    public static ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus().name(),
                product.getSalesCounter(),
                product.getReviews().stream().map(
                        ReviewService::mapToDTO).collect(Collectors.toSet()),
                product.getCategory().getId()
        );
    }
}
