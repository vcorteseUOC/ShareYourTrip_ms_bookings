package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.services;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequest;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequestStatus;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.exceptions.BookingNotFoundException;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.mappers.BookingRequestMapper;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.repositories.BookingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingRequestServiceImpl implements BookingRequestService{

    @Autowired
    private BookingRequestRepository bookingRequestRepository;

    @Override
    public BookingRequestResponseDto create(BookingRequestDto dto) {
        validateDates(dto);

        BookingRequest bookingRequest = BookingRequestMapper.toEntity(dto);
        BookingRequest saved = bookingRequestRepository.save(bookingRequest);

        return BookingRequestMapper.toDto(saved);
    }

    @Override
    public BookingRequestResponseDto findById(Long id) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("BookingRequest not found with id: " + id));

        return BookingRequestMapper.toDto(bookingRequest);
    }

    @Override
    public List<BookingRequestResponseDto> findByTravelerId(Long travelerId) {
        return bookingRequestRepository.findByTravelerId(travelerId)
                .stream()
                .map(BookingRequestMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingRequestResponseDto> findByHostId(Long hostId) {
        return bookingRequestRepository.findByHostId(hostId)
                .stream()
                .map(BookingRequestMapper::toDto)
                .toList();
    }

    @Override
    public BookingRequestResponseDto accept(Long id) {
        BookingRequest bookingRequest = getBookingRequestOrThrow(id);

        bookingRequest.setStatus(BookingRequestStatus.ACCEPTED);
        bookingRequest.setRespondedAt(LocalDateTime.now());

        return BookingRequestMapper.toDto(bookingRequestRepository.save(bookingRequest));
    }

    @Override
    public BookingRequestResponseDto reject(Long id) {
        BookingRequest bookingRequest = getBookingRequestOrThrow(id);

        bookingRequest.setStatus(BookingRequestStatus.REJECTED);
        bookingRequest.setRespondedAt(LocalDateTime.now());

        return BookingRequestMapper.toDto(bookingRequestRepository.save(bookingRequest));
    }

    @Override
    public BookingRequestResponseDto cancel(Long id) {
        BookingRequest bookingRequest = getBookingRequestOrThrow(id);

        bookingRequest.setStatus(BookingRequestStatus.CANCELLED);
        bookingRequest.setCancelledAt(LocalDateTime.now());

        return BookingRequestMapper.toDto(bookingRequestRepository.save(bookingRequest));
    }

    private BookingRequest getBookingRequestOrThrow(Long id) {
        return bookingRequestRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("BookingRequest not found with id: " + id));
    }

    private void validateDates(BookingRequestDto dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (!dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }
}
