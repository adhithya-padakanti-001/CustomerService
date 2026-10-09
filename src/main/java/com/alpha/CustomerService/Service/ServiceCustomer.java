package com.alpha.CustomerService.Service;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.alpha.CustomerService.DTO.BookingInfoDTO;
import com.alpha.CustomerService.DTO.ConfirmRideDTO;
import com.alpha.CustomerService.DTO.CustomerDTO;
import com.alpha.CustomerService.DTO.EarningSummaryDTO;
import com.alpha.CustomerService.DTO.LocationIQDirectionsDTO;
import com.alpha.CustomerService.DTO.LocationIQPlaceDTO;
import com.alpha.CustomerService.DTO.NearbyRiderDTO;
import com.alpha.CustomerService.DTO.ResponseStructure;
import com.alpha.CustomerService.DTO.RideFareDTO;
import com.alpha.CustomerService.DTO.SearchDestinationLocationResponseDTO;
import com.alpha.CustomerService.DTO.SelectRideDTO;
import com.alpha.CustomerService.DTO.SelectRideResponseDTO;
import com.alpha.CustomerService.DTO.SelectVehicleDTO;
import com.alpha.CustomerService.Entity.Booking;
import com.alpha.CustomerService.Entity.Coordinate;
import com.alpha.CustomerService.Entity.Customer;
import com.alpha.CustomerService.Repositary.BookingRepositay;
import com.alpha.CustomerService.Repositary.CoordinateRepositary;
import com.alpha.CustomerService.Repositary.CustomerRepositary;

@Service
public class ServiceCustomer {

	@Autowired
	private CustomerRepositary customerRepository;
	@Autowired
	private BookingRepositay bookingRepositay;
	@Autowired
	private CoordinateRepositary coordinateRepositary;

	// create Customer
	public ResponseStructure<CustomerDTO> createcustomer(CustomerDTO c) {

		Customer savedCustomer = new Customer();

		savedCustomer.setName(c.getName());
		savedCustomer.setEmail(c.getEmail());
		savedCustomer.setPhone(c.getPhone());
		savedCustomer.setPassword(c.getPassword());
		savedCustomer.setGender(c.getGender());

		customerRepository.save(savedCustomer);

		ResponseStructure<CustomerDTO> response = new ResponseStructure<>();

		response.setStatuscode(201);
		response.setMessage("Customer account created successfully");
		response.setData(c);

		return response;
	}

	// Find Customer
	public ResponseStructure<CustomerDTO> findcustomer(Long customerId) {

		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new RuntimeException("Customer not found with id " + customerId));

		CustomerDTO dto = new CustomerDTO();
		dto.setName(customer.getName());
		dto.setEmail(customer.getEmail());
		dto.setPhone(customer.getPhone());
		dto.setGender(customer.getGender());

		ResponseStructure<CustomerDTO> response = new ResponseStructure<>();
		response.setStatuscode(200);
		response.setMessage("Customer found");
		response.setData(dto);
		return response;
	}

	// delete Customer
	public ResponseStructure<String> deletecustomer(Long customerId) {

		ResponseStructure<String> response = new ResponseStructure<>();

		if (customerRepository.existsById(customerId)) {
			customerRepository.deleteById(customerId);

			response.setStatuscode(200);
			response.setMessage("Customer deleted successfully");
			response.setData("Deleted customer id " + customerId);
		} else {
			response.setStatuscode(404);
			response.setMessage("Customer not found with id " + customerId);
			response.setData(null);
		}
		return response;
	}

	@Value("${locationiq.api.key}")
	private String locationIqKey;

	@Value("${locationiq.autocomplete.url}")
	private String locationIqUrl;

	private final RestClient restClient = RestClient.create();

	public ResponseStructure<ArrayList<SearchDestinationLocationResponseDTO>> searchdroplocation(String searchkey) {

		ResponseStructure<ArrayList<SearchDestinationLocationResponseDTO>> response = new ResponseStructure<>();
		ArrayList<SearchDestinationLocationResponseDTO> results = new ArrayList<>();

		// Validate input
		if (searchkey == null || searchkey.trim().length() < 2) {
			response.setStatuscode(400);
			response.setMessage("Search key must be at least 2 characters");
			response.setData(results);
			return response;
		}

		URI uri = UriComponentsBuilder.fromUriString(locationIqUrl).queryParam("key", locationIqKey)
				.queryParam("q", searchkey.trim()).queryParam("limit", 5).queryParam("countrycodes", "in") // restrict
																											// to India
				.queryParam("dedupe", 1).queryParam("format", "json").build().encode().toUri();

		try {
			LocationIQPlaceDTO[] places = restClient.get().uri(uri).retrieve().body(LocationIQPlaceDTO[].class);

			if (places != null) {
				for (LocationIQPlaceDTO p : places) {
					SearchDestinationLocationResponseDTO dto = new SearchDestinationLocationResponseDTO();
					dto.setPlaceId(p.getPlaceId());
					dto.setPlaceName(p.getDisplayPlace());
					dto.setFullAddress(p.getDisplayName());
					dto.setLatitude(p.getLat() != null ? Double.valueOf(p.getLat()) : null);
					dto.setLongitude(p.getLon() != null ? Double.valueOf(p.getLon()) : null);

					if (p.getAddress() != null) {
						dto.setCity(p.getAddress().getCity());
						dto.setState(p.getAddress().getState());
						dto.setPostcode(p.getAddress().getPostcode());
					}
					results.add(dto);
				}
			}

			response.setStatuscode(200);
			response.setMessage(results.isEmpty() ? "No locations found" : "Locations fetched successfully");
			response.setData(results);

		} catch (HttpClientErrorException.NotFound e) {
			// LocationIQ returns 404 when nothing matches
			response.setStatuscode(200);
			response.setMessage("No locations found");
			response.setData(results);

		} catch (RestClientException e) {
			response.setStatuscode(502);
			response.setMessage("Unable to fetch locations from LocationIQ: " + e.getMessage());
			response.setData(results);
		}
		return response;
	}

	// ***********************
	// *****select Ride*******
	// ***********************

	@Value("${locationiq.directions.url}")
	private String locationIqDirectionsUrl;

	private static final double BIKE_RATE = 10;
	private static final double AUTO_RATE = 20;
	private static final double CAR_RATE = 30;

	public ResponseStructure<SelectRideResponseDTO> selectride(double pickupLat, double pickupLon, double dropLat,
			double dropLon) {

		ResponseStructure<SelectRideResponseDTO> response = new ResponseStructure<>();

		// lon,lat order is required by LocationIQ
		String coordinates = pickupLon + "," + pickupLat + ";" + dropLon + "," + dropLat;

		URI uri = UriComponentsBuilder.fromUriString(locationIqDirectionsUrl + "/" + coordinates)
				.queryParam("key", locationIqKey).queryParam("overview", "false").build().toUri();

		try {
			LocationIQDirectionsDTO directions = restClient.get().uri(uri).retrieve()
					.body(LocationIQDirectionsDTO.class);

			if (directions == null || directions.getRoutes() == null || directions.getRoutes().isEmpty()) {
				response.setStatuscode(404);
				response.setMessage("No route found between pickup and drop");
				response.setData(null);
				return response;
			}

			LocationIQDirectionsDTO.Route route = directions.getRoutes().get(0);
			double distanceKm = Math.round(route.getDistance() / 1000.0 * 100.0) / 100.0;
			int durationMin = (int) Math.round(route.getDuration() / 60.0);

			List<RideFareDTO> rides = new ArrayList<>();
			rides.add(new RideFareDTO("BIKE", BIKE_RATE, calculateFare(distanceKm, BIKE_RATE)));
			rides.add(new RideFareDTO("AUTO", AUTO_RATE, calculateFare(distanceKm, AUTO_RATE)));
			rides.add(new RideFareDTO("CAR", CAR_RATE, calculateFare(distanceKm, CAR_RATE)));

			SelectRideResponseDTO data = new SelectRideResponseDTO();
			data.setDistanceKm(distanceKm);
			data.setDurationMinutes(durationMin);
			data.setRides(rides);

			response.setStatuscode(200);
			response.setMessage("Ride options fetched successfully");
			response.setData(data);

		} catch (RestClientException e) {
			response.setStatuscode(502);
			response.setMessage("Unable to calculate route from LocationIQ: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

//---------------------
	private double calculateFare(double distanceKm, double ratePerKm) {
		return Math.round(distanceKm * ratePerKm * 100.0) / 100.0;
	}
//-----------------------
//	public ResponseStructure<SelectRideDTO> selectRide(SelectRideDTO s) {
//
//	    ResponseStructure<SelectRideDTO> response = new ResponseStructure<>();
//
//	    // 1. Check the customer exists
//	    if (s.getCustomerId() == null || !customerRepository.existsById(s.getCustomerId())) {
//	        response.setStatuscode(404);
//	        response.setMessage("Customer not found");
//	        response.setData(null);
//	        return response;
//	    }
//
//	    // 2. Pickup and drop must not be the same point
//	    if (s.getPickupLat() == s.getDropLat() && s.getPickupLon() == s.getDropLon()) {
//	        response.setStatuscode(400);
//	        response.setMessage("Pickup and drop locations cannot be the same");
//	        response.setData(null);
//	        return response;
//	    }
//
//	    // 3. Call LocationIQ Directions (lon,lat order)
//	    String coordinates = s.getPickupLon() + "," + s.getPickupLat() + ";"
//	                       + s.getDropLon() + "," + s.getDropLat();
//
//	    URI uri = UriComponentsBuilder.fromUriString(locationIqDirectionsUrl + "/" + coordinates)
//	            .queryParam("key", locationIqKey)
//	            .queryParam("overview", "false")
//	            .build()
//	            .toUri();
//
//	    try {
//	        LocationIQDirectionsDTO directions = restClient.get()
//	                .uri(uri)
//	                .retrieve()
//	                .body(LocationIQDirectionsDTO.class);
//
//	        if (directions == null || directions.getRoutes() == null || directions.getRoutes().isEmpty()) {
//	            response.setStatuscode(404);
//	            response.setMessage("No route found between pickup and drop");
//	            response.setData(null);
//	            return response;
//	        }
//
//	        // 4. Distance and duration
//	        LocationIQDirectionsDTO.Route route = directions.getRoutes().get(0);
//	        double distanceKm = Math.round(route.getDistance() / 1000.0 * 100.0) / 100.0;
//	        int durationMin = (int) Math.round(route.getDuration() / 60.0);
//
//	        // 5. Fares for bike, auto, car
//	        List<RideFareDTO> rides = new ArrayList<>();
//	        rides.add(new RideFareDTO("BIKE", BIKE_RATE, calculateFare(distanceKm, BIKE_RATE)));
//	        rides.add(new RideFareDTO("AUTO", AUTO_RATE, calculateFare(distanceKm, AUTO_RATE)));
//	        rides.add(new RideFareDTO("CAR",  CAR_RATE,  calculateFare(distanceKm, CAR_RATE)));
//
//	        // 6. Fill the response fields on the same DTO
//	        s.setDistanceKm(distanceKm);
//	        s.setDurationMinutes(durationMin);
//	        s.setRides(rides);
//
//	        response.setStatuscode(200);
//	        response.setMessage("Ride options fetched successfully");
//	        response.setData(s);
//
//	    } catch (RestClientException e) {
//	        response.setStatuscode(502);
//	        response.setMessage("Unable to calculate route from LocationIQ: " + e.getMessage());
//	        response.setData(null);
//	    }
//	    return response;
//	}

	public ResponseStructure<SelectRideDTO> selectRide(SelectRideDTO s) {

		ResponseStructure<SelectRideDTO> response = new ResponseStructure<>();

		// 1. Check the customer exists
		if (s.getCustomerId() == null || !customerRepository.existsById(s.getCustomerId())) {
			response.setStatuscode(404);
			response.setMessage("Customer not found");
			response.setData(null);
			return response;
		}

		// 2. Pickup and drop must not be the same point
		if (s.getPickupLat() == s.getDropLat() && s.getPickupLon() == s.getDropLon()) {
			response.setStatuscode(400);
			response.setMessage("Pickup and drop locations cannot be the same");
			response.setData(null);
			return response;
		}

		// 3. Call LocationIQ Directions (lon,lat order)
		String coordinates = s.getPickupLon() + "," + s.getPickupLat() + ";" + s.getDropLon() + "," + s.getDropLat();

		URI uri = UriComponentsBuilder.fromUriString(locationIqDirectionsUrl + "/" + coordinates)
				.queryParam("key", locationIqKey).queryParam("overview", "false").build().toUri();

		try {
			LocationIQDirectionsDTO directions = restClient.get().uri(uri).retrieve()
					.body(LocationIQDirectionsDTO.class);

			if (directions == null || directions.getRoutes() == null || directions.getRoutes().isEmpty()) {
				response.setStatuscode(404);
				response.setMessage("No route found between pickup and drop");
				response.setData(null);
				return response;
			}

			// 4. Distance and duration
			LocationIQDirectionsDTO.Route route = directions.getRoutes().get(0);
			double distanceKm = Math.round(route.getDistance() / 1000.0 * 100.0) / 100.0;
			int durationMin = (int) Math.round(route.getDuration() / 60.0);

			// 5. Fares for bike, auto, car
			List<RideFareDTO> rides = new ArrayList<>();
			rides.add(new RideFareDTO("BIKE", BIKE_RATE, calculateFare(distanceKm, BIKE_RATE)));
			rides.add(new RideFareDTO("AUTO", AUTO_RATE, calculateFare(distanceKm, AUTO_RATE)));
			rides.add(new RideFareDTO("CAR", CAR_RATE, calculateFare(distanceKm, CAR_RATE)));

			// 6. Save the quote in Redis for the next step (selectvehicle)
			String key = PENDING_RIDE_KEY + s.getCustomerId();

			Map<String, String> quote = new HashMap<>();
			quote.put("customerId", String.valueOf(s.getCustomerId()));
			quote.put("pickupLat", String.valueOf(s.getPickupLat()));
			quote.put("pickupLon", String.valueOf(s.getPickupLon()));
			quote.put("dropLat", String.valueOf(s.getDropLat()));
			quote.put("dropLon", String.valueOf(s.getDropLon()));
			quote.put("pickupLocation", s.getPickupLocation() == null ? "" : s.getPickupLocation());
			quote.put("dropLocation", s.getDropLocation() == null ? "" : s.getDropLocation());
			quote.put("distanceKm", String.valueOf(distanceKm));
			quote.put("durationMinutes", String.valueOf(durationMin));
			quote.put("BIKE", String.valueOf(rides.get(0).getFare()));
			quote.put("AUTO", String.valueOf(rides.get(1).getFare()));
			quote.put("CAR", String.valueOf(rides.get(2).getFare()));

			stringRedisTemplate.delete(key); // clear any older quote or selection for this customer
			stringRedisTemplate.opsForHash().putAll(key, quote);
			stringRedisTemplate.expire(key, PENDING_RIDE_TTL);

			// 7. Fill the response fields on the same DTO
			s.setDistanceKm(distanceKm);
			s.setDurationMinutes(durationMin);
			s.setRides(rides);

			response.setStatuscode(200);
			response.setMessage("Ride options fetched successfully");
			response.setData(s);

		} catch (RestClientException e) {
			response.setStatuscode(502);
			response.setMessage("Unable to calculate route from LocationIQ: " + e.getMessage());
			e.printStackTrace();
			response.setData(null);
		}
		return response;
	}

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private static final String PENDING_RIDE_KEY = "ride:pending:";
	private static final Duration PENDING_RIDE_TTL = Duration.ofMinutes(15);
	private static final String ACTIVE_RIDERS_KEY = "riders:active:";
	private static final String RIDER_ALIVE_KEY = "rider:alive:";
	private static final double SEARCH_RADIUS_KM = 2;
	private static final String ASSIGNED_RIDES_KEY = "assignedrides:";
	private static final Duration ASSIGNED_RIDES_TTL = Duration.ofMinutes(10);

	public ResponseStructure<SelectVehicleDTO> selectvehicle(SelectVehicleDTO v) {

		ResponseStructure<SelectVehicleDTO> response = new ResponseStructure<>();

		// 1. Validate customer
		if (v.getCustomerId() == null || !customerRepository.existsById(v.getCustomerId())) {
			response.setStatuscode(404);
			response.setMessage("Customer not found");
			response.setData(null);
			return response;
		}

		// 2. Validate vehicle type
		String type = v.getVehicleType() == null ? "" : v.getVehicleType().trim().toUpperCase();
		double rate;
		switch (type) {
		case "BIKE":
			rate = BIKE_RATE;
			break;
		case "AUTO":
			rate = AUTO_RATE;
			break;
		case "CAR":
			rate = CAR_RATE;
			break;
		default:
			response.setStatuscode(400);
			response.setMessage("Vehicle type must be BIKE, AUTO or CAR");
			response.setData(null);
			return response;
		}

		// 3. Read the quote from Redis
		String key = PENDING_RIDE_KEY + v.getCustomerId();
		Map<Object, Object> quote = stringRedisTemplate.opsForHash().entries(key);

		if (quote.isEmpty()) {
			response.setStatuscode(404);
			response.setMessage("No active ride found or it has expired. Please select pickup and drop again");
			response.setData(null);
			return response;
		}

		// 4. Pick the fare for the chosen vehicle
		double fare = Double.parseDouble((String) quote.get(type));

		// 5. Mark the selection in the same Redis entry and refresh the expiry
		stringRedisTemplate.opsForHash().put(key, "vehicleType", type);
		stringRedisTemplate.opsForHash().put(key, "fare", String.valueOf(fare));
		stringRedisTemplate.opsForHash().put(key, "status", "VEHICLE_SELECTED");
		stringRedisTemplate.expire(key, PENDING_RIDE_TTL);

		// 6. Build the response
		v.setVehicleType(type);
		v.setDistanceKm(Double.parseDouble((String) quote.get("distanceKm")));
		v.setDurationMinutes(Integer.parseInt((String) quote.get("durationMinutes")));
		v.setRatePerKm(rate);
		v.setFare(fare);

		response.setStatuscode(200);
		response.setMessage("Vehicle selected successfully");
		response.setData(v);
		return response;
	}

	// ------------------------------------------------------------------
	// Step 3: confirm ride -> create Booking -> remove Redis data
	// ------------------------------------------------------------------

	public ResponseStructure<ConfirmRideDTO> confirmride(ConfirmRideDTO c, String idempotencyKey) {

		ResponseStructure<ConfirmRideDTO> response = new ResponseStructure<>();

		// a blank key is treated as "no key"
		String idemKey = (idempotencyKey == null || idempotencyKey.isBlank()) ? null : idempotencyKey.trim();

		// 0. same key sent again -> return the booking created the first time, never a
		// second one.
		// This must run first, because the Redis quote is already deleted after the
		// first confirm.
		if (idemKey != null) {
			Booking existing = bookingRepositay.findByidempotentkey(idemKey).orElse(null);
			if (existing != null) {
				return replayResponse(existing, c);
			}
		}

		if (c.getCustomerId() == null || !customerRepository.existsById(c.getCustomerId())) {
			response.setStatuscode(404);
			response.setMessage("Customer not found");
			response.setData(null);
			return response;
		}

		String type = c.getVehicleType() == null ? "" : c.getVehicleType().trim().toUpperCase();
		if (!type.equals("BIKE") && !type.equals("AUTO") && !type.equals("CAR")) {
			response.setStatuscode(400);
			response.setMessage("Vehicle type must be BIKE, AUTO or CAR");
			response.setData(null);
			return response;
		}

		String key = PENDING_RIDE_KEY + c.getCustomerId();
		Map<Object, Object> quote = stringRedisTemplate.opsForHash().entries(key);

		if (quote.isEmpty()) {
			response.setStatuscode(404);
			response.setMessage("No active ride found or it has expired. Please select pickup and drop again");
			response.setData(null);
			return response;
		}

		// Build the Booking (Coordinate rows are saved automatically by cascade)
		Booking booking = new Booking();
		booking.setCustomerId(c.getCustomerId());
		booking.setPickuplocation((String) quote.get("pickupLocation"));
		booking.setDroplocation((String) quote.get("dropLocation"));
		booking.setSource(new Coordinate(Double.parseDouble((String) quote.get("pickupLat")),
				Double.parseDouble((String) quote.get("pickupLon"))));
		booking.setDestination(new Coordinate(Double.parseDouble((String) quote.get("dropLat")),
				Double.parseDouble((String) quote.get("dropLon"))));
		booking.setVehicleType(type);
		booking.setDistanceKm(Double.parseDouble((String) quote.get("distanceKm")));
		booking.setFare(Double.parseDouble((String) quote.get(type)));
		booking.setStatus("BOOKED");
		booking.setBookingdate(LocalDate.now());
		booking.setBookingtime(LocalDateTime.now());
		booking.setOtp(String.format("%04d", c.getCustomerId() % 10000));
		booking.setIdempotentID(idemKey); // saved, so a repeated request can find this booking

		// Save to the database FIRST
		Booking saved;
		try {
			saved = bookingRepositay.save(booking);
		} catch (DataAccessException e) {
			// two requests with the same key can arrive together: the unique column lets
			// only one
			// save succeed, so the loser returns the winner's booking instead of an error
			if (idemKey != null) {
				Booking existing = bookingRepositay.findByidempotentkey(idemKey).orElse(null);
				if (existing != null) {
					return replayResponse(existing, c);
				}
			}
			response.setStatuscode(500);
			response.setMessage("Unable to create booking. Please try again");
			response.setData(null);
			return response;
		}

		// Booking exists, so remove the temporary data from Redis
		clearPendingRide(c.getCustomerId());

		// Find active riders of the chosen vehicle type within 2 km of the pickup point
		List<NearbyRiderDTO> nearby = findNearbyRiders(type, Double.parseDouble((String) quote.get("pickupLat")),
				Double.parseDouble((String) quote.get("pickupLon")));

		// Shortlisted riders each get this booking as
		// assignedrides:{riderId}:{bookingId}
		assignRideToRiders(nearby, saved, quote);

		c.setVehicleType(type);
		c.setBookingId(saved.getBookingId());
		c.setPickupLocation(saved.getPickuplocation());
		c.setDropLocation(saved.getDroplocation());
		c.setDistanceKm(saved.getDistanceKm());
		c.setFare(saved.getFare());
		c.setStatus(saved.getStatus());
		c.setBookingTime(saved.getBookingtime());
		c.setOtp(saved.getOtp());
		c.setAvailableRiders(nearby.size());
		c.setNearbyRiders(nearby);

		String vehicle = type.toLowerCase();
		String message;
		if (nearby.isEmpty()) {
			message = "Ride confirmed. No " + vehicle + "s are available within " + (int) SEARCH_RADIUS_KM
					+ " km right now";
		} else {
			message = "Ride confirmed. " + nearby.size() + " " + vehicle + (nearby.size() == 1 ? " is" : "s are")
					+ " available within " + (int) SEARCH_RADIUS_KM + " km of your location";
		}

		response.setStatuscode(201);
		response.setMessage(message);
		response.setData(c);
		return response;
	}
	// ------------------------------------------------------------------
	// Called by the Rider service when a rider accepts a ride
	// ------------------------------------------------------------------

	public ResponseStructure<String> assignrider(Long bookingId, Long riderId) {

		ResponseStructure<String> response = new ResponseStructure<>();

		Booking booking = bookingRepositay.findById(bookingId).orElse(null);
		if (booking == null) {
			response.setStatuscode(404);
			response.setMessage("Booking not found with id " + bookingId);
			response.setData(null);
			return response;
		}

		// only a booking that is still BOOKED can be assigned
		if (!"BOOKED".equals(booking.getStatus())) {
			response.setStatuscode(409);
			response.setMessage("Booking is no longer available. Current status: " + booking.getStatus());
			response.setData(null);
			return response;
		}

		booking.setRiderId(riderId);
		booking.setStatus("RIDER_ASSIGNED");
		booking.setRiderAssignedTime(LocalDateTime.now());
		bookingRepositay.save(booking);

		response.setStatuscode(200);
		response.setMessage("Rider assigned to booking");
		response.setData("Booking " + bookingId + " assigned to rider " + riderId);
		return response;
	}

	// ------------------------------------------------------------------
	// Booking info for the Rider service (pickup, drop, status, rider)
	// ------------------------------------------------------------------

	public ResponseStructure<BookingInfoDTO> getbooking(Long bookingId) {

		ResponseStructure<BookingInfoDTO> response = new ResponseStructure<>();

		Booking booking = bookingRepositay.findById(bookingId).orElse(null);
		if (booking == null) {
			response.setStatuscode(404);
			response.setMessage("Booking not found with id " + bookingId);
			response.setData(null);
			return response;
		}

		BookingInfoDTO dto = new BookingInfoDTO();
		dto.setBookingId(booking.getBookingId());
		dto.setCustomerId(booking.getCustomerId());
		dto.setRiderId(booking.getRiderId());
		dto.setStatus(booking.getStatus());
		dto.setVehicleType(booking.getVehicleType());
		dto.setPickupLocation(booking.getPickuplocation());
		dto.setDropLocation(booking.getDroplocation());
		dto.setFare(booking.getFare());
		dto.setDistanceKm(booking.getDistanceKm());
		dto.setBookingTime(booking.getBookingtime() == null ? null : booking.getBookingtime().toString());
		dto.setPickupTime(booking.getPickuptime() == null ? null : booking.getPickuptime().toString());
		dto.setDropTime(booking.getDroptime() == null ? null : booking.getDroptime().toString());
		if (booking.getSource() != null) {
			dto.setPickupLat(booking.getSource().getLatitude());
			dto.setPickupLon(booking.getSource().getLongitue());
		}
		if (booking.getDestination() != null) {
			dto.setDropLat(booking.getDestination().getLatitude());
			dto.setDropLon(booking.getDestination().getLongitue());
		}

		response.setStatuscode(200);
		response.setMessage("Booking found");
		response.setData(dto);
		return response;
	}

	// ------------------------------------------------------------------
	// Rider reached the pickup: RIDER_ASSIGNED -> RIDER_ARRIVED
	// ------------------------------------------------------------------

	public ResponseStructure<String> markarrived(Long bookingId, Long riderId) {

		ResponseStructure<String> response = new ResponseStructure<>();

		Booking booking = bookingRepositay.findById(bookingId).orElse(null);
		if (booking == null) {
			response.setStatuscode(404);
			response.setMessage("Booking not found with id " + bookingId);
			response.setData(null);
			return response;
		}

		// only the rider who accepted this booking can update it
		if (booking.getRiderId() == null || !booking.getRiderId().equals(riderId)) {
			response.setStatuscode(403);
			response.setMessage("This booking is not assigned to rider " + riderId);
			response.setData(null);
			return response;
		}

		if (!"RIDER_ASSIGNED".equals(booking.getStatus())) {
			response.setStatuscode(409);
			response.setMessage(
					"Arrival can only be updated after accepting the ride. Current status: " + booking.getStatus());
			response.setData(null);
			return response;
		}

		booking.setStatus("RIDER_ARRIVED");
		bookingRepositay.save(booking);

		response.setStatuscode(200);
		response.setMessage("Arrival updated");
		response.setData("Booking " + bookingId + " status is now RIDER_ARRIVED");
		return response;
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	public void clearPendingRide(Long customerId) {
		stringRedisTemplate.delete(PENDING_RIDE_KEY + customerId);
	}

	// Riders of one vehicle type within SEARCH_RADIUS_KM of the pickup point.
	// Riders whose alive key has expired (app stopped sending location) are
	// skipped.
	private List<NearbyRiderDTO> findNearbyRiders(String type, double pickupLat, double pickupLon) {

		List<NearbyRiderDTO> riders = new ArrayList<>();

		try {
			GeoResults<GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(ACTIVE_RIDERS_KEY + type,
					GeoReference.fromCoordinate(pickupLon, pickupLat), // longitude first
					new Distance(SEARCH_RADIUS_KM, Metrics.KILOMETERS),
					GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().sortAscending().limit(50));

			if (results == null || results.getContent().isEmpty()) {
				return riders;
			}

			List<GeoResult<GeoLocation<String>>> found = results.getContent();

			// one Redis call to check which of the riders are still alive
			List<String> aliveKeys = new ArrayList<>();
			for (GeoResult<GeoLocation<String>> r : found) {
				aliveKeys.add(RIDER_ALIVE_KEY + r.getContent().getName());
			}
			List<String> alive = stringRedisTemplate.opsForValue().multiGet(aliveKeys);

			for (int i = 0; i < found.size(); i++) {
				if (alive != null && alive.get(i) != null) {
					GeoResult<GeoLocation<String>> r = found.get(i);
					NearbyRiderDTO dto = new NearbyRiderDTO();
					dto.setRiderId(r.getContent().getName());
					dto.setDistanceKm(Math.round(r.getDistance().getValue() * 100.0) / 100.0);
					riders.add(dto);
				}
			}
		} catch (DataAccessException e) {
			// the booking is already saved, so return an empty list instead of failing
			System.err.println("Nearby rider search failed: " + e.getMessage());
		}
		return riders;
	}

	// One Redis hash per shortlisted rider per booking:
	// assignedrides:{riderId}:{bookingId}.
	// Redis deletes it by itself after ASSIGNED_RIDES_TTL if the rider does
	// nothing.
	private void assignRideToRiders(List<NearbyRiderDTO> riders, Booking booking, Map<Object, Object> quote) {

		if (riders.isEmpty()) {
			return;
		}

		try {
			for (NearbyRiderDTO r : riders) {

				String key = ASSIGNED_RIDES_KEY + r.getRiderId() + ":" + booking.getBookingId();

				Map<String, String> ride = new HashMap<>();
				ride.put("bookingId", String.valueOf(booking.getBookingId()));
				ride.put("vehicleType", booking.getVehicleType());
				ride.put("fare", String.valueOf(booking.getFare()));
				ride.put("tripDistanceKm", String.valueOf(booking.getDistanceKm()));
				ride.put("pickupLocation", booking.getPickuplocation() == null ? "" : booking.getPickuplocation());
				ride.put("dropLocation", booking.getDroplocation() == null ? "" : booking.getDroplocation());
				ride.put("pickupLat", String.valueOf(quote.get("pickupLat")));
				ride.put("pickupLon", String.valueOf(quote.get("pickupLon")));
				ride.put("dropLat", String.valueOf(quote.get("dropLat")));
				ride.put("dropLon", String.valueOf(quote.get("dropLon")));
				ride.put("riderDistanceKm", String.valueOf(r.getDistanceKm()));

				stringRedisTemplate.opsForHash().putAll(key, ride);
				stringRedisTemplate.expire(key, ASSIGNED_RIDES_TTL);
			}
		} catch (DataAccessException e) {
			// the booking is already saved, so do not fail the confirm call
			System.err.println("Assigning ride to riders failed: " + e.getMessage());
		}
	}

	// Riders who still hold the offer for this booking: keys
	// assignedrides:{riderId}:{bookingId}
	private List<NearbyRiderDTO> getShortlistedRiders(Long bookingId) {

		List<NearbyRiderDTO> riders = new ArrayList<>();

		ScanOptions options = ScanOptions.scanOptions().match(ASSIGNED_RIDES_KEY + "*:" + bookingId).count(100).build();
		try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
			while (cursor.hasNext()) {
				String key = cursor.next(); // e.g. assignedrides:6:1
				Object distance = stringRedisTemplate.opsForHash().get(key, "riderDistanceKm");
				if (distance == null) {
					continue; // expired between the scan and the read
				}
				NearbyRiderDTO dto = new NearbyRiderDTO();
				dto.setRiderId(key.substring(ASSIGNED_RIDES_KEY.length(), key.lastIndexOf(':')));
				dto.setDistanceKm(Double.parseDouble(String.valueOf(distance)));
				riders.add(dto);
			}
		} catch (DataAccessException e) {
			System.err.println("Reading shortlisted riders failed: " + e.getMessage());
		}

		riders.sort(Comparator.comparingDouble(NearbyRiderDTO::getDistanceKm));
		return riders;
	}

	// Builds the response for a request whose idempotency key was already used
	private ResponseStructure<ConfirmRideDTO> replayResponse(Booking existing, ConfirmRideDTO request) {

		ResponseStructure<ConfirmRideDTO> response = new ResponseStructure<>();

		// same key must mean the same request: another customer or another vehicle is a
		// conflict
		boolean sameCustomer = existing.getCustomerId() != null
				&& existing.getCustomerId().equals(request.getCustomerId());
		boolean sameVehicle = request.getVehicleType() == null || request.getVehicleType().isBlank()
				|| existing.getVehicleType().equalsIgnoreCase(request.getVehicleType().trim());
		if (!sameCustomer || !sameVehicle) {
			response.setStatuscode(409);
			response.setMessage("This idempotency key was already used for a different request");
			response.setData(null);
			return response;
		}

		// riders who still hold the offer, read from the assignedrides keys
		List<NearbyRiderDTO> shortlisted = getShortlistedRiders(existing.getBookingId());

		ConfirmRideDTO cc = new ConfirmRideDTO();
		cc.setCustomerId(existing.getCustomerId());
		cc.setVehicleType(existing.getVehicleType());
		cc.setBookingId(existing.getBookingId());
		cc.setPickupLocation(existing.getPickuplocation());
		cc.setDropLocation(existing.getDroplocation());
		cc.setDistanceKm(existing.getDistanceKm());
		cc.setFare(existing.getFare());
		cc.setStatus(existing.getStatus());
		cc.setBookingTime(existing.getBookingtime());
		cc.setOtp(existing.getOtp());
		cc.setAvailableRiders(shortlisted.size());
		cc.setNearbyRiders(shortlisted);

		response.setStatuscode(200);
		response.setMessage("Booking already created for this request. Status: " + existing.getStatus());
		response.setData(cc);
		return response;
	}

	public ResponseStructure<String> verifyotp(Long bookingId, Long riderId, String otp) {

		ResponseStructure<String> response = new ResponseStructure<>();

		Booking booking = bookingRepositay.findById(bookingId).orElse(null);
		if (booking == null) {
			response.setStatuscode(404);
			response.setMessage("Booking not found with id " + bookingId);
			response.setData(null);
			return response;
		}

		// only the rider who accepted this booking can start it
		if (booking.getRiderId() == null || !booking.getRiderId().equals(riderId)) {
			response.setStatuscode(403);
			response.setMessage("This booking is not assigned to rider " + riderId);
			response.setData(null);
			return response;
		}

		// the rider must have reached the pickup first
		if (!"RIDER_ARRIVED".equals(booking.getStatus())) {
			response.setStatuscode(409);
			response.setMessage(
					"OTP can be verified only after reaching the pickup. Current status: " + booking.getStatus());
			response.setData(null);
			return response;
		}

		// compare with the OTP saved on the booking
		if (otp == null || booking.getOtp() == null || !booking.getOtp().equals(otp.trim())) {
			response.setStatuscode(400);
			response.setMessage("Invalid OTP");
			response.setData(null);
			return response;
		}

		// OTP matches, so the ride starts now
		booking.setStatus("RIDE_STARTED");
		booking.setPickuptime(LocalDateTime.now());
		bookingRepositay.save(booking);

		response.setStatuscode(200);
		response.setMessage("OTP verified. Ride started");
		response.setData("Booking " + bookingId + " status is now RIDE_STARTED");
		return response;
	}

	public ResponseStructure<BookingInfoDTO> completebooking(Long bookingId, Long riderId) {

		ResponseStructure<BookingInfoDTO> response = new ResponseStructure<>();

		Booking booking = bookingRepositay.findById(bookingId).orElse(null);
		if (booking == null) {
			response.setStatuscode(404);
			response.setMessage("Booking not found with id " + bookingId);
			response.setData(null);
			return response;
		}

		// only the rider of this booking can complete it
		if (booking.getRiderId() == null || !booking.getRiderId().equals(riderId)) {
			response.setStatuscode(403);
			response.setMessage("This booking is not assigned to rider " + riderId);
			response.setData(null);
			return response;
		}

		// the ride must have started (OTP verified)
		if (!"RIDE_STARTED".equals(booking.getStatus())) {
			response.setStatuscode(409);
			response.setMessage(
					"Ride can be completed only after it has started. Current status: " + booking.getStatus());
			response.setData(null);
			return response;
		}

		booking.setStatus("COMPLETED");
		booking.setDroptime(LocalDateTime.now());
		bookingRepositay.save(booking);

		// return the complete booking information (re-read, so it shows the saved
		// values)
		ResponseStructure<BookingInfoDTO> info = getbooking(bookingId);
		info.setMessage("Ride completed");
		return info;
	}

	// "all" -> null (no filter), "completed" -> COMPLETED, "cancelled" ->
	// CANCELLED, anything else -> "INVALID"
	private String historyStatus(String status) {
		String s = status == null ? "ALL" : status.trim().toUpperCase();
		if (s.isEmpty() || s.equals("ALL")) {
			return null;
		}
		if (s.equals("COMPLETED") || s.equals("CANCELLED")) {
			return s;
		}
		return "INVALID";
	}

	private BookingInfoDTO toBookingInfo(Booking booking) {
		BookingInfoDTO dto = new BookingInfoDTO();
		dto.setBookingId(booking.getBookingId());
		dto.setCustomerId(booking.getCustomerId());
		dto.setRiderId(booking.getRiderId());
		dto.setStatus(booking.getStatus());
		dto.setVehicleType(booking.getVehicleType());
		dto.setPickupLocation(booking.getPickuplocation());
		dto.setDropLocation(booking.getDroplocation());
		dto.setFare(booking.getFare());
		dto.setDistanceKm(booking.getDistanceKm());
		dto.setBookingTime(booking.getBookingtime() == null ? null : booking.getBookingtime().toString());
		dto.setPickupTime(booking.getPickuptime() == null ? null : booking.getPickuptime().toString());
		dto.setDropTime(booking.getDroptime() == null ? null : booking.getDroptime().toString());
		if (booking.getSource() != null) {
			dto.setPickupLat(booking.getSource().getLatitude());
			dto.setPickupLon(booking.getSource().getLongitue());
		}
		if (booking.getDestination() != null) {
			dto.setDropLat(booking.getDestination().getLatitude());
			dto.setDropLon(booking.getDestination().getLongitue());
		}
		return dto;
	}

	private ResponseStructure<List<BookingInfoDTO>> historyResponse(List<Booking> bookings, String owner) {
		List<BookingInfoDTO> list = new ArrayList<>();
		for (Booking b : bookings) {
			list.add(toBookingInfo(b));
		}

		ResponseStructure<List<BookingInfoDTO>> response = new ResponseStructure<>();
		response.setStatuscode(200);
		response.setMessage(
				list.isEmpty() ? "No bookings found for " + owner : list.size() + " booking(s) found for " + owner);
		response.setData(list);
		return response;
	}

	// customer ride history
	public ResponseStructure<List<BookingInfoDTO>> customerridehistory(Long customerId, String status) {

		ResponseStructure<List<BookingInfoDTO>> response = new ResponseStructure<>();

		String filter = historyStatus(status);
		if ("INVALID".equals(filter)) {
			response.setStatuscode(400);
			response.setMessage("Status must be all, completed or cancelled");
			response.setData(null);
			return response;
		}

		if (!customerRepository.existsById(customerId)) {
			response.setStatuscode(404);
			response.setMessage("Customer not found with id " + customerId);
			response.setData(null);
			return response;
		}

		List<Booking> bookings = filter == null ? bookingRepositay.findByCustomerIdOrderByBookingIdDesc(customerId)
				: bookingRepositay.findByCustomerIdAndStatusOrderByBookingIdDesc(customerId, filter);

		return historyResponse(bookings, "customer " + customerId);
	}

	// rider ride history (called by the Rider service)
	public ResponseStructure<List<BookingInfoDTO>> riderridehistory(Long riderId, String status) {

		ResponseStructure<List<BookingInfoDTO>> response = new ResponseStructure<>();

		String filter = historyStatus(status);
		if ("INVALID".equals(filter)) {
			response.setStatuscode(400);
			response.setMessage("Status must be all, completed or cancelled");
			response.setData(null);
			return response;
		}

		List<Booking> bookings = filter == null ? bookingRepositay.findByRiderIdOrderByBookingIdDesc(riderId)
				: bookingRepositay.findByRiderIdAndStatusOrderByBookingIdDesc(riderId, filter);

		return historyResponse(bookings, "rider " + riderId);
	}

	// ride count, total fare and total distance of a rider's COMPLETED rides
	// (called by the Rider service)
	public ResponseStructure<EarningSummaryDTO> ridersummary(Long riderId, LocalDate from, LocalDate to) {

		ResponseStructure<EarningSummaryDTO> response = new ResponseStructure<>();

		// from the start of 'from' up to the end of the 'to' day
		LocalDateTime start = from.atStartOfDay();
		LocalDateTime end = to.plusDays(1).atStartOfDay();

		BookingRepositay.EarningSummaryView view = bookingRepositay.summarizeCompleted(riderId, start, end);

		EarningSummaryDTO dto = new EarningSummaryDTO();
		dto.setRides(view == null || view.getRides() == null ? 0 : view.getRides());
		dto.setTotalFare(view == null || view.getTotalFare() == null ? 0.0 : view.getTotalFare());
		dto.setTotalDistanceKm(view == null || view.getTotalDistance() == null ? 0.0 : view.getTotalDistance());

		response.setStatuscode(200);
		response.setMessage("Summary calculated");
		response.setData(dto);
		return response;
	}

}