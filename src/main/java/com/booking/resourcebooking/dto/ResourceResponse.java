package com.booking.resourcebooking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;
}