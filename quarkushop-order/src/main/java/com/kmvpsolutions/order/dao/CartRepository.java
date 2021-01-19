package com.kmvpsolutions.order.dao;

import com.kmvpsolutions.order.domain.Cart;
import com.kmvpsolutions.order.domain.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByStatus(CartStatus status);
    List<Cart> findByStatusAndCustomer(CartStatus status, Long customer);
}
