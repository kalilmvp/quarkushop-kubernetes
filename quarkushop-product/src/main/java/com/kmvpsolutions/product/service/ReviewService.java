package com.kmvpsolutions.product.service;

import com.kmvpsolutions.commons.dto.ReviewDTO;
import com.kmvpsolutions.product.dao.ProductRepository;
import com.kmvpsolutions.product.dao.ReviewRepository;
import com.kmvpsolutions.product.domain.Product;
import com.kmvpsolutions.product.domain.Review;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
public class ReviewService {

    @Inject
    ReviewRepository reviewRepository;

    @Inject
    ProductRepository productRepository;

    public List<ReviewDTO> findAll() {
        log.debug("Request to get all reviews");

        return this.reviewRepository.findAll()
                .stream()
                .map(ReviewService::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO findById(Long id) {
        log.debug("Request to get Review: {}", id);

        return this.reviewRepository.findById(id)
                .map(ReviewService::mapToDTO).orElse(null);
    }

    public List<ReviewDTO> findReviewsByProductId(Long id) {
        log.debug("Request to get all reviwes by product id {}", id);

        return this.reviewRepository.findReviewsByProductId(id)
                .stream()
                .map(ReviewService::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO create(ReviewDTO reviewDTO, Long productId) {
        log.debug("Request to create Review: {} for the product id {}", reviewDTO, productId);

        Product product = this.productRepository.findById(productId).orElseThrow(() ->
                new IllegalStateException("Product with ID: " + productId + " was not found!"));

        Review review = this.reviewRepository.saveAndFlush(
                new Review(
                        reviewDTO.getTitle(),
                        reviewDTO.getDescription(),
                        reviewDTO.getRating()
                ));

        product.getReviews().add(review);

        this.productRepository.saveAndFlush(product);

        return mapToDTO(review);
    }

    public void delete(Long id) {
        log.debug("Request to delete Review: {}", id);

        Review review = this.reviewRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("The review with id " + id + " was not found!"));

        Product productOfTheReview = this.productRepository.findProductByReviewId(id);

        productOfTheReview.getReviews().remove(review);

        this.productRepository.saveAndFlush(productOfTheReview);
        this.reviewRepository.delete(review);
    }

    public static ReviewDTO mapToDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getTitle(),
                review.getDescription(),
                review.getRating()
        );
    }
}
