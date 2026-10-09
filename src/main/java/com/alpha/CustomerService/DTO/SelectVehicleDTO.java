package com.alpha.CustomerService.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SelectVehicleDTO {

	// ---- request fields ----
	private Long customerId;
	private String vehicleType; // BIKE, AUTO or CAR

	// ---- response fields ----
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private double distanceKm;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int durationMinutes;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private double ratePerKm;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private double fare;

	public SelectVehicleDTO() {
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

	public double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public double getRatePerKm() {
		return ratePerKm;
	}

	public void setRatePerKm(double ratePerKm) {
		this.ratePerKm = ratePerKm;
	}

	public double getFare() {
		return fare;
	}

	public void setFare(double fare) {
		this.fare = fare;
	}
}