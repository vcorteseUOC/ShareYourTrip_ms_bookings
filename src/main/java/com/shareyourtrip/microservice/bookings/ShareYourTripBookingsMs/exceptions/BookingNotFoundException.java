package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.exceptions;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String message) {
        super(message);
    }
}
