package com.kmvpsolutions.customer.domain;

import com.kmvpsolutions.commons.domain.AbstractEntity;
import com.kmvpsolutions.customer.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "payments")
public class Payment extends AbstractEntity {

    @Column(name = "transaction")
    private String transaction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @NotNull
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
}
