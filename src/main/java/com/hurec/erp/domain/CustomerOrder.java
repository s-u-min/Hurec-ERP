package com.hurec.erp.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String workDate;

    @Column(nullable = false, length = 50)
    private String customerName;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String serviceType;

    @Column(length = 50)
    private String engineerName;

    @Column(precision = 15, scale = 2)
    private BigDecimal commissionFee;

    @Column(precision = 15, scale = 2)
    private BigDecimal supplyPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal vat;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Builder
    public CustomerOrder(String workDate, String customerName, String address,
                         String serviceType, String engineerName, BigDecimal commissionFee,
                         BigDecimal supplyPrice, BigDecimal vat, BigDecimal totalAmount) {
        this.workDate = workDate;
        this.customerName = customerName;
        this.address = address;
        this.serviceType = serviceType;
        this.engineerName = engineerName;
        this.commissionFee = commissionFee;
        this.supplyPrice = supplyPrice;
        this.vat = vat;
        this.totalAmount = totalAmount;
    }
}
