package com.booking.resourcebooking.dto;

import com.booking.resourcebooking.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReservationStatusRequest {

    @NotNull(message = "Reservation status is required")
    private ReservationStatus status;

}