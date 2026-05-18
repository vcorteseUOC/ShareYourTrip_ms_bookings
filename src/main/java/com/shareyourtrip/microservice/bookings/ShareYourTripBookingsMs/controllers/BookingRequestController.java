package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.controllers;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.UserBookingStatsDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.services.BookingRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking-requests")
@RequiredArgsConstructor
public class BookingRequestController {

    @Autowired
    private BookingRequestService bookingRequestService;

    @PostMapping
    public BookingRequestResponseDto create(@Valid @RequestBody BookingRequestDto dto) {
        return bookingRequestService.create(dto);
    }

    @GetMapping("/{id}")
    public BookingRequestResponseDto findById(@PathVariable Long id) {
        return bookingRequestService.findById(id);
    }

    @GetMapping("/traveler/{travelerId}")
    public List<BookingRequestResponseDto> findByTravelerId(@PathVariable Long travelerId) {
        return bookingRequestService.findByTravelerId(travelerId);
    }

    @GetMapping("/host/{hostId}")
    public List<BookingRequestResponseDto> findByHostId(@PathVariable Long hostId) {
        return bookingRequestService.findByHostId(hostId);
    }

    @GetMapping("/accommodations")
    public Map<Long, List<Long>> getBookingRequestIdsByAccommodationIds(@RequestParam("accommodationIds") List<Long> accommodationIds) {
        return bookingRequestService.getBookingRequestIdsByAccommodationIds(accommodationIds);
    }

    @GetMapping("/accommodation/{accommodationId}")
    public List<BookingRequestResponseDto> findByAccommodationId(
        @PathVariable Long accommodationId,
        @RequestParam(required = false) String checkIn,
        @RequestParam(required = false) String checkOut
    ) {
        return bookingRequestService.findByAccommodationId(accommodationId, checkIn, checkOut);
    }

    @GetMapping("/accommodations/details")
    public Map<Long, List<BookingRequestResponseDto>> findByAccommodationIds(
        @RequestParam("accommodationIds") List<Long> accommodationIds,
        @RequestParam(required = false) String checkIn,
        @RequestParam(required = false) String checkOut
    ) {
        return bookingRequestService.findByAccommodationIds(accommodationIds, checkIn, checkOut);
    }

    @PatchMapping("/{id}/accept")
    public BookingRequestResponseDto accept(@PathVariable Long id) {
        return bookingRequestService.accept(id);
    }

    @PatchMapping("/{id}/reject")
    public BookingRequestResponseDto reject(@PathVariable Long id) {
        return bookingRequestService.reject(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingRequestResponseDto cancel(@PathVariable Long id) {
        return bookingRequestService.cancel(id);
    }

    @GetMapping("/stats/{userId}")
    public UserBookingStatsDto getUserBookingStats(@PathVariable Long userId) {
        return bookingRequestService.getUserBookingStats(userId);
    }
}