package com.kmvpsolutions.order.service;

import com.kmvpsolutions.commons.dto.OrderDTO;
import com.kmvpsolutions.commons.dto.OrderItemDTO;
import com.kmvpsolutions.order.dao.CartRepository;
import com.kmvpsolutions.order.dao.OrderRepository;
import com.kmvpsolutions.order.domain.Cart;
import com.kmvpsolutions.order.domain.Order;
import com.kmvpsolutions.order.domain.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
public class OrderService {

    @Inject
    OrderRepository orderRepository;

    @Inject
    CartRepository cartRepository;

    public List<OrderDTO> findAll() {
        log.debug("Request to get all orders");

        return this.orderRepository.findAll()
                .stream()
                .map(OrderService::mapToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO findById(Long id) {
        log.debug("Request to get Order : {}", id);
        return this.orderRepository.findById(id)
                .map(OrderService::mapToDTO).orElse(null);
    }

    public List<OrderDTO> findAllByUser(Long id) {
        return this.orderRepository.findByCartCustomerId(id)
                .stream().map(OrderService::mapToDTO).collect(Collectors.toList());
    }

    public OrderDTO create(OrderDTO orderDTO) {
        log.debug("Request to create order {}", orderDTO);

        Long cartId = orderDTO.getCart().getId();

        Cart cart = this.cartRepository.findById(cartId).orElseThrow(
                () -> new IllegalStateException("The cart with ID [" + cartId + "] was not found!")
        );

        return mapToDTO(this.orderRepository.save(
                new Order(
                        BigDecimal.ZERO,
                        OrderStatus.CREATION,
                        null,
                        null,
                        AddressService.createFromDTO(orderDTO.getShipmentAddress()),
                        Collections.emptySet(),
                        cart
                )
        ));
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete order with id {}", id);

        Order order = this.orderRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("Order with id " + id + " not found!"));

        this.orderRepository.delete(order);
    }

    public boolean existsById(Long id) {
        return this.orderRepository.existsById(id);
    }

    public static OrderDTO mapToDTO(Order order) {
        Set<OrderItemDTO> orderItems = order.getOrderItems().stream()
                .map(OrderItemService::mapToDTO).collect(Collectors.toSet());

        return new OrderDTO(
                order.getId(),
                order.getPrice(),
                order.getStatus().name(),
                order.getShipped(),
                order.getPaymentId() != null ? order.getPaymentId() : null,
                AddressService.mapToDTO(order.getShipmentAddress()),
                orderItems,
                CartService.mapToDTO(order.getCart())
        );
    }
}
