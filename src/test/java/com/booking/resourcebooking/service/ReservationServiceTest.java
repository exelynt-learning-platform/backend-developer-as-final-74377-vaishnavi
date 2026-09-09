package com.booking.resourcebooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.dto.ReservationResponse;
import com.booking.resourcebooking.entity.Reservation;
import com.booking.resourcebooking.entity.Resource;
import com.booking.resourcebooking.entity.User;
import com.booking.resourcebooking.exception.BadRequestException;
import com.booking.resourcebooking.exception.ResourceNotFoundException;
import com.booking.resourcebooking.repository.ReservationRepository;
import com.booking.resourcebooking.repository.ResourceRepository;
import com.booking.resourcebooking.repository.UserRepository;

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
    
    @Test
    void shouldCreateReservationSuccessfully() {

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
                .available(true)
                .build();

        User user = User.builder()
                .id(10L)
                .username("user")
                .build();

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        Reservation savedReservation = Reservation.builder()
                .id(100L)
                .user(user)
                .resource(resource)
                .price(resource.getPrice())
                .status(com.booking.resourcebooking.enums.ReservationStatus.PENDING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        var response = reservationService.createReservation(
                request,
                "user"
        );

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("user", response.getUsername());
        assertEquals(1L, response.getResourceId());
        assertEquals(
                new java.math.BigDecimal("1000"),
                response.getPrice()
        );
        assertEquals(
                com.booking.resourcebooking.enums.ReservationStatus.PENDING,
                response.getStatus()
        );

        verify(resourceRepository).findById(1L);
        verify(userRepository).findByUsername("user");
        verify(reservationRepository).save(any(Reservation.class));
    }
    
    @Test
    void shouldRejectReservationWhenUserDoesNotExist() {

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
                .available(true)
                .build();

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.createReservation(
                        request,
                        "unknown"
                )
        );

        verify(resourceRepository).findById(1L);
        verify(userRepository).findByUsername("unknown");
        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }
    
    @Test
    void shouldAllowOwnerToViewReservation() {

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

        User user = User.builder()
                .id(10L)
                .username("user")
                .email("user@gmail.com")
                .password("password")
                .role(com.booking.resourcebooking.enums.Role.USER)
                .build();

        Resource resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .price(new java.math.BigDecimal("1000"))
                .available(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(user)
                .resource(resource)
                .price(new java.math.BigDecimal("1000"))
                .status(com.booking.resourcebooking.enums.ReservationStatus.PENDING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        var response = reservationService.getReservationById(
                100L,
                "user",
                false
        );

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("user", response.getUsername());
        assertEquals(1L, response.getResourceId());

        verify(reservationRepository).findById(100L);
    }
    @Test
    void shouldRejectDifferentUserFromViewingReservation() {

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

        User owner = User.builder()
                .id(10L)
                .username("owner")
                .email("owner@gmail.com")
                .password("password")
                .role(com.booking.resourcebooking.enums.Role.USER)
                .build();

        Resource resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .price(new java.math.BigDecimal("1000"))
                .available(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(owner)
                .resource(resource)
                .price(new java.math.BigDecimal("1000"))
                .status(com.booking.resourcebooking.enums.ReservationStatus.PENDING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> reservationService.getReservationById(
                        100L,
                        "differentUser",
                        false
                )
        );

        verify(reservationRepository).findById(100L);
    }
    
    @Test
    void shouldAllowAdminToViewAnyReservation() {

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

        User owner = User.builder()
                .id(10L)
                .username("owner")
                .email("owner@gmail.com")
                .password("password")
                .role(com.booking.resourcebooking.enums.Role.USER)
                .build();

        Resource resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .price(new java.math.BigDecimal("1000"))
                .available(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(owner)
                .resource(resource)
                .price(new java.math.BigDecimal("1000"))
                .status(com.booking.resourcebooking.enums.ReservationStatus.PENDING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        var response = reservationService.getReservationById(
                100L,
                "admin",
                true
        );

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("owner", response.getUsername());

        verify(reservationRepository).findById(100L);
    }
    
    @Test
    void shouldUpdateReservationSuccessfully() {

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

        User user = User.builder()
                .id(10L)
                .username("user")
                .email("user@gmail.com")
                .password("password")
                .role(com.booking.resourcebooking.enums.Role.USER)
                .build();

        Resource oldResource = Resource.builder()
                .id(1L)
                .name("Old Room")
                .price(new java.math.BigDecimal("1000"))
                .available(true)
                .build();

        Resource newResource = Resource.builder()
                .id(2L)
                .name("New Room")
                .price(new java.math.BigDecimal("1500"))
                .available(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(user)
                .resource(oldResource)
                .price(oldResource.getPrice())
                .status(com.booking.resourcebooking.enums.ReservationStatus.PENDING)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        when(resourceRepository.findById(2L))
                .thenReturn(Optional.of(newResource));

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservation);

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(2L);
        request.setStartTime(LocalDateTime.now().plusHours(3));
        request.setEndTime(LocalDateTime.now().plusHours(4));
        request.setStatus(
                com.booking.resourcebooking.enums.ReservationStatus.CONFIRMED
        );

        var response = reservationService.updateReservation(
                100L,
                request
        );

        assertNotNull(response);
        assertEquals(100L, response.getId());

        assertEquals(2L, reservation.getResource().getId());
        assertEquals(
                new java.math.BigDecimal("1500"),
                reservation.getPrice()
        );

        assertEquals(
                com.booking.resourcebooking.enums.ReservationStatus.CONFIRMED,
                reservation.getStatus()
        );

        verify(reservationRepository).findById(100L);
        verify(resourceRepository).findById(2L);
        verify(reservationRepository).save(any(Reservation.class));
    }
    @Test
    void shouldRejectUpdateWhenReservationDoesNotExist() {

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

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.updateReservation(
                        100L,
                        request
                )
        );

        verify(reservationRepository).findById(100L);
        verify(resourceRepository, never()).findById(anyLong());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
    @Test
    void shouldRejectUpdateWhenResourceDoesNotExist() {

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

        User user = User.builder()
                .id(10L)
                .username("user")
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(user)
                .build();

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        when(resourceRepository.findById(2L))
                .thenReturn(Optional.empty());

        ReservationRequest request = new ReservationRequest();

        request.setResourceId(2L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.updateReservation(
                        100L,
                        request
                )
        );

        verify(reservationRepository).findById(100L);
        verify(resourceRepository).findById(2L);
        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }
    
    
    @Test
    void shouldDeleteReservationSuccessfully() {

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

        User user = User.builder()
                .id(10L)
                .username("user")
                .build();

        Resource resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .price(new java.math.BigDecimal("1000"))
                .available(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(user)
                .resource(resource)
                .price(new java.math.BigDecimal("1000"))
                .status(
                        com.booking.resourcebooking.enums.ReservationStatus.PENDING
                )
                .build();

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(100L);

        verify(reservationRepository).findById(100L);
        verify(reservationRepository).delete(reservation);
    }
    
    @Test
    void shouldRejectDeleteWhenReservationDoesNotExist() {

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

        when(reservationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.deleteReservation(999L)
        );

        verify(reservationRepository).findById(999L);

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }
    
    @Test
    void shouldGetAllReservationsSuccessfully() {

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

        org.springframework.data.domain.Page<Reservation> reservationPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(reservationRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(reservationPage);

        org.springframework.data.domain.Page<ReservationResponse> response =
                reservationService.getAllReservations(
                        null,
                        null,
                        null,
                        org.springframework.data.domain.PageRequest.of(0, 10)
                );

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());

        verify(reservationRepository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    @Test
    void shouldGetAllReservationsWithStatusFilter() {

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

        org.springframework.data.domain.Page<Reservation> reservationPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(reservationRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(reservationPage);

        org.springframework.data.domain.Page<ReservationResponse> response =
                reservationService.getAllReservations(
                        com.booking.resourcebooking.enums.ReservationStatus.PENDING,
                        null,
                        null,
                        org.springframework.data.domain.PageRequest.of(0, 10)
                );

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());

        verify(reservationRepository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    @Test
    void shouldGetAllReservationsWithMinimumPriceFilter() {

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

        org.springframework.data.domain.Page<Reservation> reservationPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(reservationRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(reservationPage);

        org.springframework.data.domain.Page<ReservationResponse> response =
                reservationService.getAllReservations(
                        null,
                        new java.math.BigDecimal("500"),
                        null,
                        org.springframework.data.domain.PageRequest.of(0, 10)
                );

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());

        verify(reservationRepository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    @Test
    void shouldGetAllReservationsWithMaximumPriceFilter() {

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

        org.springframework.data.domain.Page<Reservation> reservationPage =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of()
                );

        when(reservationRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(reservationPage);

        org.springframework.data.domain.Page<ReservationResponse> response =
                reservationService.getAllReservations(
                        null,
                        null,
                        new java.math.BigDecimal("2000"),
                        org.springframework.data.domain.PageRequest.of(0, 10)
                );

        assertNotNull(response);
        assertEquals(0, response.getTotalElements());

        verify(reservationRepository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    @Test
    void shouldUpdateReservationStatusSuccessfully() {

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

        User user = User.builder()
                .id(10L)
                .username("user")
                .build();

        Resource resource = Resource.builder()
                .id(1L)
                .name("Conference Room")
                .price(new java.math.BigDecimal("1000"))
                .available(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(100L)
                .user(user)
                .resource(resource)
                .price(new java.math.BigDecimal("1000"))
                .status(
                        com.booking.resourcebooking.enums.ReservationStatus.PENDING
                )
                .startTime(LocalDateTime.of(2026, 9, 10, 10, 0))
                .endTime(LocalDateTime.of(2026, 9, 10, 12, 0))
                .build();

        ReservationRequest request = new ReservationRequest();
        request.setResourceId(1L);
        request.setStartTime(
                LocalDateTime.of(2026, 9, 10, 14, 0)
        );
        request.setEndTime(
                LocalDateTime.of(2026, 9, 10, 16, 0)
        );
        request.setStatus(
                com.booking.resourcebooking.enums.ReservationStatus.CONFIRMED
        );

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.save(reservation))
                .thenReturn(reservation);

        ReservationResponse response =
                reservationService.updateReservation(
                        100L,
                        request
                );

        assertNotNull(response);
        assertEquals(
                com.booking.resourcebooking.enums.ReservationStatus.CONFIRMED,
                reservation.getStatus()
        );

        verify(reservationRepository).save(reservation);
    }
}