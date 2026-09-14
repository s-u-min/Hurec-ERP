package com.hurec.erp.service;

import com.hurec.erp.domain.CustomerOrder;
import com.hurec.erp.domain.CustomerOrderRepository;
import com.hurec.erp.dto.CustomerOrderDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;

    @Transactional
    public int saveCustomerOrders(List<CustomerOrderDto> dtoList) {
        List<CustomerOrder> orderEntities = dtoList.stream()
                .map(dto -> CustomerOrder.builder()
                        .workDate(dto.getWorkDate())
                        .customerName(dto.getCustomerName())
                        .address(dto.getAddress())
                        .serviceType(dto.getServiceType())
                        .engineerName(dto.getEngineerName())
                        .commissionFee(dto.getCommissionFee())
                        .supplyPrice(dto.getSupplyPrice())
                        .vat(dto.getVat())
                        .totalAmount(dto.getTotalAmount())
                        .build())
                .toList();

        List<CustomerOrder> savedList = customerOrderRepository.saveAll(orderEntities);
        return savedList.size();
    }
}
