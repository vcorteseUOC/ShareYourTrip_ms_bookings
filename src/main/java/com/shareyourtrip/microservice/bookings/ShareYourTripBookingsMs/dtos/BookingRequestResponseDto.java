package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestResponseDto {

    private Long id;
    private Long accommodationId;
    private Long travelerId;
    private Long hostId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer guestsCount;
    private String message;
    private BookingRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime cancelledAt;
}