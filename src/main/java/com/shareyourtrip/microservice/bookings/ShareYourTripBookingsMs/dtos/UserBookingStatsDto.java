package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookingStatsDto {
    private Long totalBookings;
    private Long pendingRequests;
    private Long completedBookings;
}
