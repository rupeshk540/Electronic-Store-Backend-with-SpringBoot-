package com.electronic.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatsDto {


    private long totalOrders;

    private double totalRevenue;

    private long placedOrders;

    private long deliveredOrders;
}
