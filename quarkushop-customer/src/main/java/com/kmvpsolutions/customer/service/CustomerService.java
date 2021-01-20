package com.kmvpsolutions.customer.service;

import com.kmvpsolutions.commons.dto.CustomerDTO;
import com.kmvpsolutions.customer.dao.CustomerRepository;
import com.kmvpsolutions.customer.domain.Customer;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
public class CustomerService {

    @Inject
    CustomerRepository customerRepository;

    public List<CustomerDTO> findAll() {
        log.debug("Request to get all customers");

        return this.customerRepository.findAll()
                .stream()
                .map(CustomerService::mapToDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO findById(Long id) {
        log.debug("Request to get Customer {}", id);

        return this.customerRepository.findById(id).map(CustomerService::mapToDTO).orElse(null);
    }

    public List<CustomerDTO> findAllActive() {
        log.debug("Request to get all active customers");

        return this.customerRepository.findAllByEnabled(Boolean.TRUE)
                .stream()
                .map(CustomerService::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerDTO> findAllInactive() {
        log.debug("Request to get all inactive customers");

        return this.customerRepository.findAllByEnabled(Boolean.FALSE)
                .stream()
                .map(CustomerService::mapToDTO)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        log.debug("Request to delete customer {)", id);

        Customer customer = this.customerRepository.findById(id).orElseThrow(() ->
                new IllegalStateException("Cannot find customer with id {}" + id));

        customer.setEnabled(Boolean.FALSE);

        this.customerRepository.save(customer);
    }

    public CustomerDTO create(CustomerDTO customerDTO) {
        log.debug("Request to create Customer {}", customerDTO);

        return mapToDTO(this.customerRepository.save(
                new Customer(
                    customerDTO.getFirstName(),
                        customerDTO.getLastName(),
                        customerDTO.getEmail(),
                        customerDTO.getTelephone(),
                        Boolean.TRUE
                )
        ));
    }

    public static CustomerDTO mapToDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getTelephone()
        );
    }
}
