package com.electronic.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

        private long products;
        private long categories;
        private long collections;
        private long users;
        private long orders;
        private long pendingOrders;
        private long lowStock;
}
