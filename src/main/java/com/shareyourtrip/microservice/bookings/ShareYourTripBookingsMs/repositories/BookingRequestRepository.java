package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.repositories;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequest;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

    List<BookingRequest> findByTravelerId(Long travelerId);

    List<BookingRequest> findByHostId(Long hostId);

    List<BookingRequest> findByAccommodationId(Long accommodationId);

    List<BookingRequest> findByStatus(BookingRequestStatus status);

    List<BookingRequest> findByTravelerIdAndStatus(Long travelerId, BookingRequestStatus status);

    List<BookingRequest> findByHostIdAndStatus(Long hostId, BookingRequestStatus status);
}