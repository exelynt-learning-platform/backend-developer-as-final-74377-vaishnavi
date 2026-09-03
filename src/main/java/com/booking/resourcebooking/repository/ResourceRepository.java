package com.booking.resourcebooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booking.resourcebooking.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}