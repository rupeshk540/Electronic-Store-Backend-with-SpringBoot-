package com.electronic.store.entities.enums;

public enum PaymentStatus {

    PENDING,    // initiated but not completed
    SUCCESS,    // payment completed successfully
    FAILED,     // payment failed
    CANCELLED   // payment cancelled by user
}
