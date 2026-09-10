package com.booking.resourcebooking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.dto.ReservationResponse;
import com.booking.resourcebooking.dto.UpdateReservationStatusRequest;
import com.booking.resourcebooking.entity.Reservation;
import com.booking.resourcebooking.entity.Resource;
import com.booking.resourcebooking.entity.User;
import com.booking.resourcebooking.enums.ReservationStatus;
import com.booking.resourcebooking.exception.BadRequestException;
import com.booking.resourcebooking.exception.ResourceNotFoundException;
import com.booking.resourcebooking.repository.ReservationRepository;
import com.booking.resourcebooking.repository.ResourceRepository;
import com.booking.resourcebooking.repository.UserRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    // USER/ADMIN - Create reservation
    public ReservationResponse createReservation(
            ReservationRequest request,
            String username) {

        validateTimeRange(request.getStartTime(), request.getEndTime());
        Resource resource = getAvailableResource(request.getResourceId());

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .price(resource.getPrice())
                .status(ReservationStatus.PENDING)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        return mapToResponse(reservationRepository.save(reservation));
    }

    public Page<ReservationResponse> getMyReservations(
            String username,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return reservationRepository.findAll(
                (root, query, criteriaBuilder) -> {
                    Predicate predicates = buildReservationPredicates(
                            root, criteriaBuilder, status, minPrice, maxPrice);

                    return criteriaBuilder.and(
                            predicates,
                            criteriaBuilder.equal(root.get("user").get("id"), user.getId())
                    );
                },
                pageable
        ).map(this::mapToResponse);
    }

    // USER/ADMIN - Get reservation by ID
    public ReservationResponse getReservationById(
            Long id,
            String username,
            boolean isAdmin) {

        Reservation reservation = getExistingReservation(id);

        if (!isAdmin && !reservation.getUser().getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to view this reservation"
            );
        }

        return mapToResponse(reservation);
    }

    public Page<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        return reservationRepository.findAll(
                (root, query, criteriaBuilder) -> buildReservationPredicates(
                        root, criteriaBuilder, status, minPrice, maxPrice),
                pageable
        ).map(this::mapToResponse);
    }

    // ADMIN - Update reservation details
    public ReservationResponse updateReservation(
            Long id,
            ReservationRequest request) {

        Reservation reservation = getExistingReservation(id);
        validateTimeRange(request.getStartTime(), request.getEndTime());
        Resource resource = getAvailableResource(request.getResourceId());

        reservation.setResource(resource);
        reservation.setPrice(resource.getPrice());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        
        // Removed request.getStatus() mapping here to fix the compilation error

        return mapToResponse(reservationRepository.save(reservation));
    }

    // ADMIN - Update reservation status ONLY (New Method)
    public ReservationResponse updateReservationStatus(
            Long id,
            UpdateReservationStatusRequest request) {

        Reservation reservation = getExistingReservation(id);
        reservation.setStatus(request.getStatus());

        return mapToResponse(reservationRepository.save(reservation));
    }

    // ADMIN - Delete reservation
    public void deleteReservation(Long id) {
        Reservation reservation = getExistingReservation(id);
        reservationRepository.delete(reservation);
    }

    // --- Private Helper Methods (Fixes the "Long Method" code smell) ---

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private Resource getAvailableResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + resourceId));

        if (!resource.isAvailable()) {
            throw new BadRequestException("Resource is currently unavailable");
        }
        return resource;
    }

    private Reservation getExistingReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id: " + id));
    }

    private Predicate buildReservationPredicates(
            Root<Reservation> root,
            CriteriaBuilder criteriaBuilder,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        Predicate predicates = criteriaBuilder.conjunction();

        if (status != null) {
            predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.equal(root.get("status"), status)
            );
        }

        if (minPrice != null) {
            predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice)
            );
        }

        if (maxPrice != null) {
            predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice)
            );
        }

        return predicates;
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getUsername(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getStartTime(),
                reservation.getEndTime()
        );
    }
}