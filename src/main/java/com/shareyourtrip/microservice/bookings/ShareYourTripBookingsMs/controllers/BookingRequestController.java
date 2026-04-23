package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.controllers;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.services.BookingRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-requests")
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
}