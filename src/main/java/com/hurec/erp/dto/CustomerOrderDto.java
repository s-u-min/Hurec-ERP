package com.hurec.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CustomerOrderDto {

    private String workDate;
    private String customerName;
    private String address;
    private String serviceType;
    private String engineerName;

    private BigDecimal commissionFee;
    private BigDecimal supplyPrice;
    private BigDecimal vat;
    private BigDecimal totalAmount;

    public void updateCommissionFee(BigDecimal newFee) {
        this.commissionFee = newFee;
    }
}
