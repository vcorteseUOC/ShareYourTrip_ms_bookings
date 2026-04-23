package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.services;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequest;

import java.util.List;

public interface BookingRequestService {
    public BookingRequestResponseDto create(BookingRequestDto dto);
    public BookingRequestResponseDto findById(Long id);
    public List<BookingRequestResponseDto> findByTravelerId(Long travelerId);
    public List<BookingRequestResponseDto> findByHostId(Long hostId);
    public BookingRequestResponseDto accept(Long id);
    public BookingRequestResponseDto reject(Long id);
    public BookingRequestResponseDto cancel(Long id);
}
