package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.mappers;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequest;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequestStatus;

public class BookingRequestMapper {

    private BookingRequestMapper() {
    }

    public static BookingRequest toEntity(BookingRequestDto dto) {
        return BookingRequest.builder()
                .accommodationId(dto.getAccommodationId())
                .travelerId(dto.getTravelerId())
                .hostId(dto.getHostId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .guestsCount(dto.getGuestsCount())
                .message(dto.getMessage())
                .status(BookingRequestStatus.PENDING)
                .build();
    }

    public static BookingRequestResponseDto toDto(BookingRequest entity) {
        return BookingRequestResponseDto.builder()
                .id(entity.getId())
                .accommodationId(entity.getAccommodationId() != null ? entity.getAccommodationId() : null)
                .travelerId(entity.getTravelerId())
                .hostId(entity.getHostId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .guestsCount(entity.getGuestsCount())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .respondedAt(entity.getRespondedAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }
}
