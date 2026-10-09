package com.alpha.CustomerService.DTO;

public class EarningSummaryDTO {

	private long rides;
	private double totalFare;
	private double totalDistanceKm;

	public EarningSummaryDTO() {
		super();
	}

	public long getRides() {
		return rides;
	}

	public void setRides(long rides) {
		this.rides = rides;
	}

	public double getTotalFare() {
		return totalFare;
	}

	public void setTotalFare(double totalFare) {
		this.totalFare = totalFare;
	}

	public double getTotalDistanceKm() {
		return totalDistanceKm;
	}

	public void setTotalDistanceKm(double totalDistanceKm) {
		this.totalDistanceKm = totalDistanceKm;
	}
}