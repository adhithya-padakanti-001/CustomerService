package com.alpha.CustomerService.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Booking {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long bookingId;
	private Long customerId;
	private String pickuplocation;
	private String droplocation;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "source_id")
	private Coordinate source;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "destination_id")
	private Coordinate destination;

	private String vehicleType;
	private double distanceKm;
	private double fare;
	private String status;
	
	@ManyToOne(cascade = CascadeType.ALL)
	private Customer customer;

	// filled when a rider accepts the ride
	private Long riderId;
	private LocalDateTime riderAssignedTime;

	private LocalDate bookingdate;
	private LocalDateTime bookingtime;
	private LocalDateTime pickuptime;
	private LocalDateTime droptime;

	private String otp;

	@Column(name = "idempotency_key", nullable = false, unique = true)
	private String idempotentkey;

	public String getIdempotentID() {
		return idempotentkey;
	}

	public void setIdempotentID(String idempotentID) {
		this.idempotentkey = idempotentID;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public Booking() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Booking(Long customerId, String pickuplocation, String droplocation, Coordinate source,
			Coordinate destination, LocalDate bookingdate, LocalDateTime bookingtime, LocalDateTime pickuptime,
			LocalDateTime droptime) {
		super();
		this.customerId = customerId;
		this.pickuplocation = pickuplocation;
		this.droplocation = droplocation;
		this.source = source;
		this.destination = destination;
		this.bookingdate = bookingdate;
		this.bookingtime = bookingtime;
		this.pickuptime = pickuptime;
		this.droptime = droptime;
	}

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getPickuplocation() {
		return pickuplocation;
	}

	public void setPickuplocation(String pickuplocation) {
		this.pickuplocation = pickuplocation;
	}

	public String getDroplocation() {
		return droplocation;
	}

	public void setDroplocation(String droplocation) {
		this.droplocation = droplocation;
	}

	public Coordinate getSource() {
		return source;
	}

	public void setSource(Coordinate source) {
		this.source = source;
	}

	public Coordinate getDestination() {
		return destination;
	}

	public void setDestination(Coordinate destination) {
		this.destination = destination;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}

	public double getFare() {
		return fare;
	}

	public void setFare(double fare) {
		this.fare = fare;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getRiderId() {
		return riderId;
	}

	public void setRiderId(Long riderId) {
		this.riderId = riderId;
	}

	public LocalDateTime getRiderAssignedTime() {
		return riderAssignedTime;
	}

	public void setRiderAssignedTime(LocalDateTime riderAssignedTime) {
		this.riderAssignedTime = riderAssignedTime;
	}

	public LocalDate getBookingdate() {
		return bookingdate;
	}

	public void setBookingdate(LocalDate bookingdate) {
		this.bookingdate = bookingdate;
	}

	public LocalDateTime getBookingtime() {
		return bookingtime;
	}

	public void setBookingtime(LocalDateTime bookingtime) {
		this.bookingtime = bookingtime;
	}

	public LocalDateTime getPickuptime() {
		return pickuptime;
	}

	public void setPickuptime(LocalDateTime pickuptime) {
		this.pickuptime = pickuptime;
	}

	public LocalDateTime getDroptime() {
		return droptime;
	}

	public void setDroptime(LocalDateTime droptime) {
		this.droptime = droptime;
	}

}