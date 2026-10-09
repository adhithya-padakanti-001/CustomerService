package com.alpha.CustomerService.DTO;

public class RideFareDTO {

	private String vehicleType;
	private double ratePerKm;
	private double fare;

	public RideFareDTO() {
		super();
	}

	public RideFareDTO(String vehicleType, double ratePerKm, double fare) {
		this.vehicleType = vehicleType;
		this.ratePerKm = ratePerKm;
		this.fare = fare;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
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