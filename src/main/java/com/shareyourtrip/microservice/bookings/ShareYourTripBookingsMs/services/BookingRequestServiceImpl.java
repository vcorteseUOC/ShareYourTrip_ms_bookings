package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.services;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.UserBookingStatsDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequest;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequestStatus;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.exceptions.BookingNotFoundException;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.integration.ReviewClient;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.mappers.BookingRequestMapper;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.repositories.BookingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingRequestServiceImpl implements BookingRequestService{

    @Autowired
    private BookingRequestRepository bookingRequestRepository;

    @Autowired(required = false)
    private ReviewClient reviewClient;

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
        List<BookingRequestResponseDto> bookings = bookingRequestRepository.findByTravelerId(travelerId)
                .stream()
                .map(BookingRequestMapper::toDto)
                .toList();

        if (reviewClient != null && !bookings.isEmpty()) {
            populateReviewInfo(bookings);
        }

        return bookings;
    }

    @Override
    public List<BookingRequestResponseDto> findByHostId(Long hostId) {
        List<BookingRequestResponseDto> bookings = bookingRequestRepository.findByHostId(hostId)
                .stream()
                .map(BookingRequestMapper::toDto)
                .toList();

        if (reviewClient != null && !bookings.isEmpty()) {
            populateReviewInfo(bookings);
        }

        return bookings;
    }

    @Override
    public List<BookingRequestResponseDto> findByAccommodationId(Long accommodationId, String checkIn, String checkOut) {
        List<BookingRequest> bookings = bookingRequestRepository.findByAccommodationId(accommodationId);
        
        // Filtrar solo las reservas aceptadas
        List<BookingRequest> acceptedBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingRequestStatus.ACCEPTED)
                .toList();
        
        // Filtrar por fechas si se proporcionan
        if (checkIn != null && checkOut != null) {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);
            
            acceptedBookings = acceptedBookings.stream()
                    .filter(booking -> {
                        LocalDate bookingStartDate = booking.getStartDate();
                        LocalDate bookingEndDate = booking.getEndDate();
                        // Verificar solapamiento: bookingStartDate < checkOutDate && bookingEndDate > checkInDate
                        return bookingStartDate.isBefore(checkOutDate) && bookingEndDate.isAfter(checkInDate);
                    })
                    .toList();
        }
        
        return acceptedBookings.stream()
                .map(BookingRequestMapper::toDto)
                .toList();
    }

    @Override
    public Map<Long, List<BookingRequestResponseDto>> findByAccommodationIds(List<Long> accommodationIds, String checkIn, String checkOut) {
        List<BookingRequest> bookings = bookingRequestRepository.findByAccommodationIdIn(accommodationIds);
        
        // Filtrar solo las reservas aceptadas
        List<BookingRequest> acceptedBookings = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingRequestStatus.ACCEPTED)
                .toList();
        
        // Filtrar por fechas si se proporcionan
        if (checkIn != null && checkOut != null) {
            LocalDate checkInDate = LocalDate.parse(checkIn);
            LocalDate checkOutDate = LocalDate.parse(checkOut);
            
            acceptedBookings = acceptedBookings.stream()
                    .filter(booking -> {
                        LocalDate bookingStartDate = booking.getStartDate();
                        LocalDate bookingEndDate = booking.getEndDate();
                        // Verificar solapamiento: bookingStartDate < checkOutDate && bookingEndDate > checkInDate
                        return bookingStartDate.isBefore(checkOutDate) && bookingEndDate.isAfter(checkInDate);
                    })
                    .toList();
        }
        
        return acceptedBookings.stream()
                .collect(Collectors.groupingBy(
                    BookingRequest::getAccommodationId,
                    Collectors.mapping(BookingRequestMapper::toDto, Collectors.toList())
                ));
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

    @Override
    public Map<Long, List<Long>> getBookingRequestIdsByAccommodationIds(List<Long> accommodationIds) {
        List<BookingRequest> bookings = bookingRequestRepository.findByAccommodationIdIn(accommodationIds);
        
        return bookings.stream()
                .collect(Collectors.groupingBy(
                    BookingRequest::getAccommodationId,
                    Collectors.mapping(BookingRequest::getId, Collectors.toList())
                ));
    }

    @Override
    public UserBookingStatsDto getUserBookingStats(Long userId) {
        // Obtener todas las reservas del usuario (como viajero y como host)
        List<BookingRequest> travelerBookings = bookingRequestRepository.findByTravelerId(userId);
        List<BookingRequest> hostBookings = bookingRequestRepository.findByHostId(userId);
        
        // Calcular estadísticas
        long pendingRequests = travelerBookings.stream()
                .filter(booking -> booking.getStatus() == BookingRequestStatus.PENDING)
                .count() + 
                hostBookings.stream()
                .filter(booking -> booking.getStatus() == BookingRequestStatus.PENDING)
                .count();
        
        long completedBookings = travelerBookings.stream()
                .filter(booking -> booking.getStatus() == BookingRequestStatus.COMPLETED)
                .count() + 
                hostBookings.stream()
                .filter(booking -> booking.getStatus() == BookingRequestStatus.COMPLETED)
                .count();
        
        long totalBookings = travelerBookings.size() + hostBookings.size();
        
        return UserBookingStatsDto.builder()
                .totalBookings(totalBookings)
                .pendingRequests(pendingRequests)
                .completedBookings(completedBookings)
                .build();
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

    private void populateReviewInfo(List<BookingRequestResponseDto> bookings) {
        try {
            if (reviewClient == null) {
                return;
            }

            List<Long> bookingIds = bookings.stream()
                    .map(BookingRequestResponseDto::getId)
                    .toList();

            String bookingIdsStr = bookingIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            Map<Long, Boolean> travelerReviewsExists = reviewClient.getTravelerReviewsExists(bookingIdsStr);
            Map<Long, Boolean> hostReviewsExists = reviewClient.getHostReviewsExists(bookingIdsStr);

            for (BookingRequestResponseDto booking : bookings) {
                booking.setHasTravelerReview(travelerReviewsExists.getOrDefault(booking.getId(), false));
                booking.setHasHostReview(hostReviewsExists.getOrDefault(booking.getId(), false));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener información de reviews: " + e.getMessage());
        }
    }
}
