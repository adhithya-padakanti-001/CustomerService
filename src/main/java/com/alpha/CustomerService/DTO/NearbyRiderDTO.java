package com.alpha.CustomerService.DTO;

public class NearbyRiderDTO {

	private String riderId;
	private double distanceKm;

	public NearbyRiderDTO() {
		super();
	}

	public String getRiderId() {
		return riderId;
	}

	public void setRiderId(String riderId) {
		this.riderId = riderId;
	}

	public double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}
}