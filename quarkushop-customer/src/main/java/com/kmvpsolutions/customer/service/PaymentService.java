package com.kmvpsolutions.customer.service;

import com.kmvpsolutions.commons.dto.OrderDTO;
import com.kmvpsolutions.commons.dto.PaymentDTO;
import com.kmvpsolutions.customer.client.OrderRestClient;
import com.kmvpsolutions.customer.dao.PaymentRepository;
import com.kmvpsolutions.customer.domain.Payment;
import com.kmvpsolutions.customer.domain.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    @RestClient
    OrderRestClient orderRestClient;

    public List<PaymentDTO> findAll() {
        return this.paymentRepository.findAll()
                .stream()
                .map(payment -> findById(payment.getId()))
                .collect(Collectors.toList());
    }

    public PaymentDTO findById(Long id) {
        log.debug("Request to get payment by id {}", id);

        OrderDTO order = this.orderRestClient.findByPaymentId(id).orElseThrow(() ->
                new IllegalStateException("The order does not exist"));

        return this.paymentRepository.findById(id)
                .map(payment -> mapToDTO(payment, order.getId())).orElse(null);
    }

    public List<PaymentDTO> findByPriceRange(Double max) {
        return this.paymentRepository.findAllByAmountBetween(
                BigDecimal.ZERO,
                BigDecimal.valueOf(max))
            .stream()
            .map(payment -> mapToDTO(payment,
                    this.findOrderByPaymentId(payment.getId()).getId()))
            .collect(Collectors.toList());
    }

    public PaymentDTO create(PaymentDTO paymentDTO) {
        log.debug("Request to create payment {}", paymentDTO);

        OrderDTO orderDTO = this.orderRestClient.findById(paymentDTO.getOrderId()).orElseThrow(() ->
                new IllegalStateException("The order does not exist"));
        orderDTO.setStatus("PAID");

        Payment paymentSaved = this.paymentRepository.saveAndFlush(new Payment(
                paymentDTO.getPaypalPaymentId(),
                PaymentStatus.valueOf(paymentDTO.getStatus()),
                orderDTO.getTotalPrice()
        ));

        this.orderRestClient.save(orderDTO);

        return mapToDTO(paymentSaved, orderDTO.getId());
    }

    public void delete(Long id) {
        log.debug("Request to delete payment {}", id);

        this.paymentRepository.deleteById(id);
    }

    private OrderDTO findOrderByPaymentId(Long paymentId) {
        return this.orderRestClient.findByPaymentId(paymentId).orElseThrow(() ->
                new IllegalStateException("No order exists for payment id " + paymentId));
    }

    private static PaymentDTO mapToDTO(Payment payment, Long orderId) {
        if (payment != null) {
            return new PaymentDTO(
                    payment.getId(),
                    payment.getTransaction(),
                    payment.getStatus().name(),
                    orderId
            );
        }
        return null;
    }
}
