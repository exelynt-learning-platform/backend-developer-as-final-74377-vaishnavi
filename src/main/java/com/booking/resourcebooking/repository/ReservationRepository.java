package com.booking.resourcebooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.booking.resourcebooking.entity.Reservation;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>,
                JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByUserId(Long userId);

    boolean existsByResourceId(Long resourceId);
}