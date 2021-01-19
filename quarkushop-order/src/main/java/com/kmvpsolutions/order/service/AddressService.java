package com.kmvpsolutions.order.service;

import com.kmvpsolutions.commons.dto.AddressDTO;
import com.kmvpsolutions.order.domain.Address;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressService {

    public static Address createFromDTO(AddressDTO addressDTO) {
        return new Address(
                addressDTO.getAddress1(),
                addressDTO.getAddress2(),
                addressDTO.getCity(),
                addressDTO.getPostcode(),
                addressDTO.getCountry()
        );
    }

    public static AddressDTO mapToDTO(Address address) {
        return new AddressDTO(
                address.getAddress1(),
                address.getAddress2(),
                address.getCity(),
                address.getPostcode(),
                address.getCountry()
        );
    }
}
