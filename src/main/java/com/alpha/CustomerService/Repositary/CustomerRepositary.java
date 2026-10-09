package com.alpha.CustomerService.Repositary;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alpha.CustomerService.Entity.Customer;

public interface CustomerRepositary extends JpaRepository<Customer, Long> {

}
