package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "review-service",
        url = "${services.reviews.base-url}"
)
public interface ReviewClient {

    @GetMapping("/traveler-reviews/exists")
    Map<Long, Boolean> getTravelerReviewsExists(@RequestParam("bookingRequestIds") String bookingRequestIds);

    @GetMapping("/host-reviews/exists")
    Map<Long, Boolean> getHostReviewsExists(@RequestParam("bookingRequestIds") String bookingRequestIds);
}
