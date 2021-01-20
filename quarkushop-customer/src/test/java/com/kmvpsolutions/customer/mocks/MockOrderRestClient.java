package com.kmvpsolutions.customer.mocks;

import com.kmvpsolutions.commons.dto.OrderDTO;
import com.kmvpsolutions.customer.client.OrderRestClient;
import io.quarkus.test.Mock;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.Optional;

@Mock
@ApplicationScoped
@RestClient
public class MockOrderRestClient implements OrderRestClient {

    @Override
    public Optional<OrderDTO> findById(Long id) {
        final OrderDTO order = new OrderDTO();
        order.setId(id);
        order.setTotalPrice(BigDecimal.valueOf(1000));

        return Optional.of(order);
    }

    @Override
    public Optional<OrderDTO> findByPaymentId(Long id) {
        final OrderDTO order = new OrderDTO();
        order.setId(5l);

        return Optional.of(order);
    }

    @Override
    public OrderDTO save(OrderDTO orderDTO) {
        return orderDTO;
    }
}
