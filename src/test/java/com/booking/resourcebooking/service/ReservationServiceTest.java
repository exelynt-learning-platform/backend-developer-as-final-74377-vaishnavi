package com.booking.resourcebooking.service;

import com.booking.resourcebooking.entity.Resource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.booking.resourcebooking.exception.ResourceNotFoundException;
import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.exception.BadRequestException;
import com.booking.resourcebooking.repository.ReservationRepository;
import com.booking.resourcebooking.repository.ResourceRepository;
import com.booking.resourcebooking.repository.UserRepository;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;

class ReservationServiceTest {

    @Test
    void shouldRejectReservationWhenEndTimeIsBeforeStartTime() {

        ReservationRepository reservationRepository =
                mock(ReservationRepository.class);

        ResourceRepository resourceRepository =
                mock(ResourceRepository.class);

        UserRepository userRepository =
                mock(UserRepository.class);

        ReservationService reservationService =
                new ReservationService(
                        reservationRepository,
                        resourceRepository,
                        userRepository
                );

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(
                LocalDateTime.now().plusHours(2)
        );
        request.setEndTime(
                LocalDateTime.now().plusHours(1)
        );

        assertThrows(
                BadRequestException.class,
                () -> reservationService.createReservation(
                        request,
                        "user"
                )
        );
    }
    
    @Test
    void shouldRejectReservationWhenResourceIsUnavailable() {

        ReservationRepository reservationRepository =
                mock(ReservationRepository.class);

        ResourceRepository resourceRepository =
                mock(ResourceRepository.class);

        UserRepository userRepository =
                mock(UserRepository.class);

        ReservationService reservationService =
                new ReservationService(
                        reservationRepository,
                        resourceRepository,
                        userRepository
                );

        Resource resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .price(new java.math.BigDecimal("1000"))
                .available(false)
                .build();

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(
                LocalDateTime.now().plusHours(1)
        );
        request.setEndTime(
                LocalDateTime.now().plusHours(2)
        );

        assertThrows(
                BadRequestException.class,
                () -> reservationService.createReservation(
                        request,
                        "user"
                )
        );
    }
    
    @Test
    void shouldRejectReservationWhenResourceDoesNotExist() {

        ReservationRepository reservationRepository =
                mock(ReservationRepository.class);

        ResourceRepository resourceRepository =
                mock(ResourceRepository.class);

        UserRepository userRepository =
                mock(UserRepository.class);

        ReservationService reservationService =
                new ReservationService(
                        reservationRepository,
                        resourceRepository,
                        userRepository
                );

        when(resourceRepository.findById(999L))
                .thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(999L);
        request.setStartTime(
                LocalDateTime.now().plusHours(1)
        );
        request.setEndTime(
                LocalDateTime.now().plusHours(2)
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.createReservation(
                        request,
                        "user"
                )
        );
    }
}