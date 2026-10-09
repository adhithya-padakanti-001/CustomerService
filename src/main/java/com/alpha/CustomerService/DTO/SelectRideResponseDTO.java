package com.alpha.CustomerService.DTO;

import java.util.List;

public class SelectRideResponseDTO {

	private double distanceKm;
	private int durationMinutes;
	private List<RideFareDTO> rides;

	public SelectRideResponseDTO() {
		super();
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