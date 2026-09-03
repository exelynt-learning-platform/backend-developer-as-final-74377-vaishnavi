package com.booking.resourcebooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.booking.resourcebooking.enums.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long resourceId;
    private String resourceName;
    private BigDecimal price;
    private ReservationStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}