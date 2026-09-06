package com.booking.resourcebooking.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booking.resourcebooking.dto.ReservationRequest;
import com.booking.resourcebooking.dto.ReservationResponse;
import com.booking.resourcebooking.entity.Reservation;
import com.booking.resourcebooking.entity.Resource;
import com.booking.resourcebooking.entity.User;
import com.booking.resourcebooking.enums.ReservationStatus;
import com.booking.resourcebooking.exception.BadRequestException;
import com.booking.resourcebooking.exception.ResourceNotFoundException;
import com.booking.resourcebooking.repository.ReservationRepository;
import com.booking.resourcebooking.repository.ResourceRepository;
import com.booking.resourcebooking.repository.UserRepository;

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

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException(
                    "End time must be after start time"
            );
        }

        Resource resource = resourceRepository
                .findById(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: "
                                        + request.getResourceId()
                        )
                );

        if (!resource.isAvailable()) {
            throw new BadRequestException(
                    "Resource is currently unavailable"
            );
        }

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .price(resource.getPrice())
                .status(ReservationStatus.PENDING)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }

    // USER - Get only their reservations
//    public Page<ReservationResponse> getMyReservations(
//            String username,
//            Pageable pageable) {
//
//        User user = userRepository
//                .findByUsername(username)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "User not found"
//                        )
//                );
//
//        return reservationRepository
//                .findAll(
//                        (root, query, criteriaBuilder) ->
//                                criteriaBuilder.equal(
//                                        root.get("user").get("id"),
//                                        user.getId()
//                                ),
//                        pageable
//                )
//                .map(this::mapToResponse);
//    }
    
    
    public Page<ReservationResponse> getMyReservations(
            String username,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        return reservationRepository.findAll(
                (root, query, criteriaBuilder) -> {

                    var predicates = criteriaBuilder.conjunction();

                    predicates = criteriaBuilder.and(
                            predicates,
                            criteriaBuilder.equal(
                                    root.get("user").get("id"),
                                    user.getId()
                            )
                    );

                    if (status != null) {
                        predicates = criteriaBuilder.and(
                                predicates,
                                criteriaBuilder.equal(
                                        root.get("status"),
                                        status
                                )
                        );
                    }

                    if (minPrice != null) {
                        predicates = criteriaBuilder.and(
                                predicates,
                                criteriaBuilder.greaterThanOrEqualTo(
                                        root.get("price"),
                                        minPrice
                                )
                        );
                    }

                    if (maxPrice != null) {
                        predicates = criteriaBuilder.and(
                                predicates,
                                criteriaBuilder.lessThanOrEqualTo(
                                        root.get("price"),
                                        maxPrice
                                )
                        );
                    }

                    return predicates;
                },
                pageable
        ).map(this::mapToResponse);
    }

    // USER/ADMIN - Get reservation by ID
    public ReservationResponse getReservationById(
            Long id,
            String username,
            boolean isAdmin) {

        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id
                        )
                );

        if (!isAdmin &&
                !reservation.getUser()
                        .getUsername()
                        .equals(username)) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to view this reservation"
            );
        }

        return mapToResponse(reservation);
    }

    // ADMIN - Get all reservations
//    public Page<ReservationResponse> getAllReservations(
//            Pageable pageable) {
//
//        return reservationRepository
//                .findAll(pageable)
//                .map(this::mapToResponse);
//    }
    
    
    public Page<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        return reservationRepository.findAll(
                (root, query, criteriaBuilder) -> {

                    var predicates = criteriaBuilder.conjunction();

                    if (status != null) {
                        predicates = criteriaBuilder.and(
                                predicates,
                                criteriaBuilder.equal(
                                        root.get("status"),
                                        status
                                )
                        );
                    }

                    if (minPrice != null) {
                        predicates = criteriaBuilder.and(
                                predicates,
                                criteriaBuilder.greaterThanOrEqualTo(
                                        root.get("price"),
                                        minPrice
                                )
                        );
                    }

                    if (maxPrice != null) {
                        predicates = criteriaBuilder.and(
                                predicates,
                                criteriaBuilder.lessThanOrEqualTo(
                                        root.get("price"),
                                        maxPrice
                                )
                        );
                    }

                    return predicates;
                },
                pageable
        ).map(this::mapToResponse);
    }

    // ADMIN - Update reservation
    public ReservationResponse updateReservation(
            Long id,
            ReservationRequest request) {

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException(
                    "End time must be after start time"
            );
        }

        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id
                        )
                );

        Resource resource = resourceRepository
                .findById(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: "
                                        + request.getResourceId()
                        )
                );

        if (!resource.isAvailable()) {
            throw new BadRequestException(
                    "Resource is currently unavailable"
            );
        }

//        reservation.setResource(resource);
//        reservation.setPrice(resource.getPrice());
//        reservation.setStartTime(request.getStartTime());
//        reservation.setEndTime(request.getEndTime());

        
        reservation.setResource(resource);
        reservation.setPrice(resource.getPrice());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());

        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }
        
        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    // ADMIN - Delete reservation
    public void deleteReservation(Long id) {

        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id
                        )
                );

        reservationRepository.delete(reservation);
    }

    private ReservationResponse mapToResponse(
            Reservation reservation) {

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