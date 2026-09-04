package com.booking.resourcebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

	private boolean success;
    private String message;
    private String token;
    
}