package com.alpha.CustomerService.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpha.CustomerService.DTO.BookingInfoDTO;
import com.alpha.CustomerService.DTO.ConfirmRideDTO;
import com.alpha.CustomerService.DTO.CustomerDTO;
import com.alpha.CustomerService.DTO.EarningSummaryDTO;
import com.alpha.CustomerService.DTO.ResponseStructure;
import com.alpha.CustomerService.DTO.SearchDestinationLocationResponseDTO;
import com.alpha.CustomerService.DTO.SelectRideDTO;
import com.alpha.CustomerService.DTO.SelectVehicleDTO;
import com.alpha.CustomerService.Service.ServiceCustomer;

@RestController
public class ControllerCustomer {

	@Autowired
	ServiceCustomer serviceCustomer;

	@PostMapping("/customer/createaccount")
	public ResponseStructure<CustomerDTO> createcustomer(@RequestBody CustomerDTO c) {
		return serviceCustomer.createcustomer(c);
	}

	@GetMapping("/customer/find/{customerId}")
	public ResponseStructure<CustomerDTO> findcustomer(@PathVariable Long customerId) {
		return serviceCustomer.findcustomer(customerId);
	}

	@DeleteMapping("/customer/delete/{customerId}")
	public ResponseStructure<String> deletecustomer(@PathVariable Long customerId) {
		return serviceCustomer.deletecustomer(customerId);
	}

	@GetMapping("/customer/search/droplocation")
	public ResponseStructure<ArrayList<SearchDestinationLocationResponseDTO>> searchdroplocation(
			@RequestParam String searchkey) {
		return serviceCustomer.searchdroplocation(searchkey);
	}

//	@GetMapping("/customer/selectride")
//	public ResponseStructure<SelectRideResponseDTO> selectride(@RequestParam double pickupLat,
//			@RequestParam double pickupLon, @RequestParam double dropLat, @RequestParam double dropLon) {
//		return serviceCustomer.selectride(pickupLat, pickupLon, dropLat, dropLon);
//	}

	@PostMapping("/customer/selectride")
	public ResponseStructure<SelectRideDTO> selectRide(@RequestBody SelectRideDTO s) {
		return serviceCustomer.selectRide(s);
	}

	@PostMapping("/customer/selectvehicle")
	public ResponseStructure<SelectVehicleDTO> selectvehicle(@RequestBody SelectVehicleDTO selectVehicleDTO) {
		return serviceCustomer.selectvehicle(selectVehicleDTO);
	}

	@PostMapping("/customer/confirmride")
	public ResponseStructure<ConfirmRideDTO> confirmride(@RequestBody ConfirmRideDTO c,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
		return serviceCustomer.confirmride(c, idempotencyKey);
	}

	@PutMapping("/customer/booking/assignrider/{bookingId}/{riderId}")
	public ResponseStructure<String> assignrider(@PathVariable Long bookingId, @PathVariable Long riderId) {
		return serviceCustomer.assignrider(bookingId, riderId);
	}

	@GetMapping("/customer/booking/{bookingId}")
	public ResponseStructure<BookingInfoDTO> getbooking(@PathVariable Long bookingId) {
		return serviceCustomer.getbooking(bookingId);
	}

	@PutMapping("/customer/booking/arrived/{bookingId}/{riderId}")
	public ResponseStructure<String> markarrived(@PathVariable Long bookingId, @PathVariable Long riderId) {
		return serviceCustomer.markarrived(bookingId, riderId);
	}

	@PutMapping("/customer/booking/verifyotp/{bookingId}/{riderId}")
	public ResponseStructure<String> verifyotp(@PathVariable Long bookingId, @PathVariable Long riderId,
			@RequestParam String otp) {
		return serviceCustomer.verifyotp(bookingId, riderId, otp);
	}

	@PutMapping("/customer/booking/complete/{bookingId}/{riderId}")
	public ResponseStructure<BookingInfoDTO> completebooking(@PathVariable Long bookingId, @PathVariable Long riderId) {
		return serviceCustomer.completebooking(bookingId, riderId);
	}

	@GetMapping("/customer/ridehistory")
	public ResponseStructure<List<BookingInfoDTO>> customerridehistory(@RequestParam Long customerId,
			@RequestParam(defaultValue = "all") String status) {
		return serviceCustomer.customerridehistory(customerId, status);
	}

	@GetMapping("/customer/booking/history/rider")
	public ResponseStructure<List<BookingInfoDTO>> riderridehistory(@RequestParam Long riderId,
			@RequestParam(defaultValue = "all") String status) {
		return serviceCustomer.riderridehistory(riderId, status);
	}

	@GetMapping("/customer/booking/summary/rider")
	public ResponseStructure<EarningSummaryDTO> ridersummary(@RequestParam Long riderId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return serviceCustomer.ridersummary(riderId, from, to);
	}

}