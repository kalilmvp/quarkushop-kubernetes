package com.kmvpsolutions.order;

import com.kmvpsolutions.commons.dto.AddressDTO;
import com.kmvpsolutions.order.domain.Address;
import com.kmvpsolutions.order.service.AddressService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressServiceUnitTest {

    @Test
    void testCreateFromDTO() {
        Address address =
                new Address("Rua dos Generais", null, "Luanda", "000000", "AO");

        AddressDTO addressDTO = AddressService.mapToDTO(address);

        assertThat(addressDTO).isNotNull()
                .matches(dto -> dto.getAddress1().equals("Rua dos Generais"))
                .matches(dto -> dto.getCity().equals("Luanda"))
                .matches(dto -> dto.getPostcode().equals("000000"))
                .matches(dto -> dto.getCountry().equals("AO"));
    }

    @Test
    void testMapToDTO() {
        AddressDTO addressDTO =
                new AddressDTO("Rua dos Generais", null, "Luanda", "000000", "AO");

        Address address = AddressService.createFromDTO(addressDTO);

        assertThat(address).isNotNull()
                .matches(addr -> addr.getAddress1().equals("Rua dos Generais"))
                .matches(addr -> addr.getCity().equals("Luanda"))
                .matches(addr -> addr.getPostcode().equals("000000"))
                .matches(addr -> addr.getCountry().equals("AO"));
    }
}
