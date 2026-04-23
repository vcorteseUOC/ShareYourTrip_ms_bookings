package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.mappers;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequestStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BookingRequestStatusConverter implements AttributeConverter<BookingRequestStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingRequestStatus attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public BookingRequestStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BookingRequestStatus.valueOf(dbData.toUpperCase());
    }
}
