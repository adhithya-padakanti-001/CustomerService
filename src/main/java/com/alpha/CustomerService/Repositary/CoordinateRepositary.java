package com.alpha.CustomerService.Repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alpha.CustomerService.Entity.Coordinate;

public interface CoordinateRepositary extends JpaRepository<Coordinate, Integer> {

}
