package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.services;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.UserBookingStatsDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequest;

import java.util.List;
import java.util.Map;

public interface BookingRequestService {
    public BookingRequestResponseDto create(BookingRequestDto dto);
    public BookingRequestResponseDto findById(Long id);
    public List<BookingRequestResponseDto> findByTravelerId(Long travelerId);
    public List<BookingRequestResponseDto> findByHostId(Long hostId);
    public List<BookingRequestResponseDto> findByAccommodationId(Long accommodationId, String checkIn, String checkOut);
    public Map<Long, List<BookingRequestResponseDto>> findByAccommodationIds(List<Long> accommodationIds, String checkIn, String checkOut);
    public BookingRequestResponseDto accept(Long id);
    public BookingRequestResponseDto reject(Long id);
    public BookingRequestResponseDto cancel(Long id);
    public Map<Long, List<Long>> getBookingRequestIdsByAccommodationIds(List<Long> accommodationIds);
    public UserBookingStatsDto getUserBookingStats(Long userId);
}
