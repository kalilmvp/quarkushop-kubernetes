package com.kmvpsolutions.order.service;

import com.kmvpsolutions.order.dao.CartRepository;
import com.kmvpsolutions.commons.dto.CartDTO;
import com.kmvpsolutions.order.domain.Cart;
import com.kmvpsolutions.order.domain.enums.CartStatus;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
public class CartService {

    @Inject
    CartRepository cartRepository;

    public List<CartDTO> findAll() {
        log.debug("Request to get all carts");

        return this.cartRepository.findAll()
                .stream()
                .map(CartService::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CartDTO> findAllActiveCarts() {
        log.debug("Request to get all active carts");

        return this.cartRepository.findByStatus(CartStatus.NEW)
                .stream()
                .map(CartService::mapToDTO)
                .collect(Collectors.toList());
    }

    private Cart create(Long customerId) {
        if (this.getActiveCart(customerId) == null) {
            var cart = new Cart(customerId, CartStatus.NEW);

            return this.cartRepository.save(cart);
        } else {
            throw new IllegalStateException("There is already an active cart");
        }
    }

    public CartDTO findById(Long id) {
        log.debug("Request to get cart: {}", id);
        return this.cartRepository.findById(id).map(CartService::mapToDTO).orElse(null);
    }

    public void delete(Long id) {
        log.debug("Request to delete cart: {}", id);
        Cart cart = this.cartRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Cannot find cart with the id " + id));

        cart.setStatus(CartStatus.CANCELED);

        this.cartRepository.save(cart);

    }

    public CartDTO getActiveCart(Long customerId) {
        List<Cart> activeCarts = this.cartRepository.findByStatusAndCustomer(CartStatus.NEW, customerId);

        if (activeCarts != null) {
            if (activeCarts.size() == 1) {
                return mapToDTO(activeCarts.get(0));
            }

            if (activeCarts.size() > 1) {
                throw new IllegalStateException("Many active carts detected!!");
            }
        }

        return null;
    }

    public CartDTO createDTO(Long customerId) {
        return mapToDTO(this.create(customerId));
    }

    public static CartDTO mapToDTO(Cart cart) {
        return new CartDTO(cart.getId(),
                cart.getCustomer(),
                cart.getStatus().name());

    }
}
