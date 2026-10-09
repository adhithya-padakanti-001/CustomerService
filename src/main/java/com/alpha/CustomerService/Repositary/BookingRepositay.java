package com.alpha.CustomerService.Repositary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alpha.CustomerService.Entity.Booking;

public interface BookingRepositay extends JpaRepository<Booking, Long> {

	Optional<Booking> findByidempotentkey(String idempotentkey);

	List<Booking> findByCustomerIdOrderByBookingIdDesc(Long customerId);

	List<Booking> findByCustomerIdAndStatusOrderByBookingIdDesc(Long customerId, String status);

	List<Booking> findByRiderIdOrderByBookingIdDesc(Long riderId);

	List<Booking> findByRiderIdAndStatusOrderByBookingIdDesc(Long riderId, String status);

	// one query for the whole summary: ride count, total fare and total distance of
	// COMPLETED rides
	@Query("select count(b) as rides, coalesce(sum(b.fare), 0.0) as totalFare, "
			+ "coalesce(sum(b.distanceKm), 0.0) as totalDistance from Booking b "
			+ "where b.riderId = :riderId and b.status = 'COMPLETED' "
			+ "and b.droptime >= :start and b.droptime < :end")
	EarningSummaryView summarizeCompleted(@Param("riderId") Long riderId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	interface EarningSummaryView {
		Long getRides();

		Double getTotalFare();

		Double getTotalDistance();
	}

}
