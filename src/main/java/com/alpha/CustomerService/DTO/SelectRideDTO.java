package com.alpha.CustomerService.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SelectRideDTO {

	// ---- request fields (sent by the app) ----
	private Long customerId;
	private double pickupLat;
	private double pickupLon;
	private double dropLat;
	private double dropLon;
	private String pickupLocation;
	private String dropLocation;

	// ---- response fields (filled by the service) ----
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private double distanceKm;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int durationMinutes;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private List<RideFareDTO> rides;

	public SelectRideDTO() {
		super();
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public double getPickupLat() {
		return pickupLat;
	}

	public void setPickupLat(double pickupLat) {
		this.pickupLat = pickupLat;
	}

	public double getPickupLon() {
		return pickupLon;
	}

	public void setPickupLon(double pickupLon) {
		this.pickupLon = pickupLon;
	}

	public double getDropLat() {
		return dropLat;
	}

	public void setDropLat(double dropLat) {
		this.dropLat = dropLat;
	}

	public double getDropLon() {
		return dropLon;
	}

	public void setDropLon(double dropLon) {
		this.dropLon = dropLon;
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

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public List<RideFareDTO> getRides() {
		return rides;
	}

	public void setRides(List<RideFareDTO> rides) {
		this.rides = rides;
	}
}