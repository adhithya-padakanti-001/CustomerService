package com.alpha.CustomerService.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfirmRideDTO {

	// ---- request fields (sent by the app) ----
	private Long customerId;
	private String vehicleType; // BIKE, AUTO or CAR

	// ---- response fields (filled by the service) ----
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long bookingId;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String pickupLocation;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String dropLocation;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private double distanceKm;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private double fare;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String status;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private LocalDateTime bookingTime;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int availableRiders;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private List<NearbyRiderDTO> nearbyRiders;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String otp;
	

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public ConfirmRideDTO() {
		super();
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public String getPickupLocation() {
		return pickupLocation;
	}

	public void setPickupLocation(String pickupLocation) {
		this.pickupLocation = pickupLocation;
	}

	public String getDropLocation() {
		return dropLocation;
	}

	public void setDropLocation(String dropLocation) {
		this.dropLocation = dropLocation;
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

	public LocalDateTime getBookingTime() {
		return bookingTime;
	}

	public void setBookingTime(LocalDateTime bookingTime) {
		this.bookingTime = bookingTime;
	}

	public int getAvailableRiders() {
		return availableRiders;
	}

	public void setAvailableRiders(int availableRiders) {
		this.availableRiders = availableRiders;
	}

	public List<NearbyRiderDTO> getNearbyRiders() {
		return nearbyRiders;
	}

	public void setNearbyRiders(List<NearbyRiderDTO> nearbyRiders) {
		this.nearbyRiders = nearbyRiders;
	}
}