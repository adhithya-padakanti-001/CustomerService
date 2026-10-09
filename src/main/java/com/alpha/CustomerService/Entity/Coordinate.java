package com.alpha.CustomerService.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Coordinate {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private double latitude;
	private double longitue;

	public Coordinate() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Coordinate(double latitude, double longitue) {
		super();
		this.latitude = latitude;
		this.longitue = longitue;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitue() {
		return longitue;
	}

	public void setLongitue(double longitue) {
		this.longitue = longitue;
	}

}
