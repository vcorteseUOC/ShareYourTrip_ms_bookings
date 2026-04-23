package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDto {

    @NotNull
    private Long accommodationId;

    @NotNull
    private Long travelerId;

    @NotNull
    private Long hostId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @Min(1)
    @Builder.Default
    private Integer guestsCount = 1;

    private String message;
}
