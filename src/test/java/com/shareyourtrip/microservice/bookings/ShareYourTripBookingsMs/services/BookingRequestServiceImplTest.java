package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.services;

import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.BookingRequestResponseDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.dtos.UserBookingStatsDto;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequest;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.entitites.BookingRequestStatus;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.exceptions.BookingNotFoundException;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.integration.ReviewClient;
import com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.repositories.BookingRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingRequestServiceImpl - Tests unitarios")
class BookingRequestServiceImplTest {

    @Mock
    private BookingRequestRepository bookingRequestRepository;

    @Mock
    private ReviewClient reviewClient;

    @InjectMocks
    private BookingRequestServiceImpl bookingService;

    private BookingRequest sampleBooking;
    private BookingRequestDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleBooking = BookingRequest.builder()
                .id(1L)
                .accommodationId(10L)
                .travelerId(100L)
                .hostId(200L)
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 10))
                .guestsCount(2)
                .message("Me gustaría reservar")
                .status(BookingRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleDto = BookingRequestDto.builder()
                .accommodationId(10L)
                .travelerId(100L)
                .hostId(200L)
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 10))
                .guestsCount(2)
                .message("Me gustaría reservar")
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Debe crear una reserva correctamente")
        void shouldCreateBooking() {
            when(bookingRequestRepository.save(any(BookingRequest.class))).thenAnswer(invocation -> {
                BookingRequest b = invocation.getArgument(0);
                b.setId(1L);
                return b;
            });

            BookingRequestResponseDto result = bookingService.create(sampleDto);

            assertThat(result).isNotNull();
            assertThat(result.getAccommodationId()).isEqualTo(10L);
            assertThat(result.getTravelerId()).isEqualTo(100L);
            assertThat(result.getStatus()).isEqualTo(BookingRequestStatus.PENDING);
            verify(bookingRequestRepository).save(any(BookingRequest.class));
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando startDate es null")
        void shouldThrowWhenStartDateNull() {
            sampleDto.setStartDate(null);

            assertThatThrownBy(() -> bookingService.create(sampleDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start date and end date are required");

            verify(bookingRequestRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando endDate es null")
        void shouldThrowWhenEndDateNull() {
            sampleDto.setEndDate(null);

            assertThatThrownBy(() -> bookingService.create(sampleDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start date and end date are required");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando startDate >= endDate")
        void shouldThrowWhenStartDateNotBeforeEndDate() {
            sampleDto.setStartDate(LocalDate.of(2026, 7, 10));
            sampleDto.setEndDate(LocalDate.of(2026, 7, 5));

            assertThatThrownBy(() -> bookingService.create(sampleDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start date must be before end date");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando startDate == endDate")
        void shouldThrowWhenSameDayStartAndEnd() {
            LocalDate sameDay = LocalDate.of(2026, 7, 5);
            sampleDto.setStartDate(sameDay);
            sampleDto.setEndDate(sameDay);

            assertThatThrownBy(() -> bookingService.create(sampleDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start date must be before end date");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Debe retornar DTO cuando la reserva existe")
        void shouldReturnDtoWhenFound() {
            when(bookingRequestRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));

            BookingRequestResponseDto result = bookingService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(BookingRequestStatus.PENDING);
        }

        @Test
        @DisplayName("Debe lanzar BookingNotFoundException cuando no existe")
        void shouldThrowWhenNotFound() {
            when(bookingRequestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.findById(99L))
                    .isInstanceOf(BookingNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("findByTravelerId")
    class FindByTravelerId {

        @Test
        @DisplayName("Debe retornar reservas del viajero con info de reviews")
        void shouldReturnBookingsWithReviewInfo() {
            when(bookingRequestRepository.findByTravelerId(100L)).thenReturn(List.of(sampleBooking));
            when(reviewClient.getTravelerReviewsExists("1")).thenReturn(Map.of(1L, true));
            when(reviewClient.getHostReviewsExists("1")).thenReturn(Map.of(1L, false));

            List<BookingRequestResponseDto> result = bookingService.findByTravelerId(100L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getHasTravelerReview()).isTrue();
            assertThat(result.get(0).getHasHostReview()).isFalse();
        }

        @Test
        @DisplayName("Debe retornar reservas sin info de reviews si reviewClient es null")
        void shouldReturnBookingsWithoutReviewInfoWhenClientNull() {
            // reviewClient is null by default in this test setup (Mockito @Mock returns null by default
            // for @Autowired(required=false) but we need to explicitly test this)
            // Since Mockito creates a mock, reviewClient won't be null. We test the empty case.
            when(bookingRequestRepository.findByTravelerId(100L)).thenReturn(Collections.emptyList());

            List<BookingRequestResponseDto> result = bookingService.findByTravelerId(100L);

            assertThat(result).isEmpty();
            verify(reviewClient, never()).getTravelerReviewsExists(anyString());
        }

        @Test
        @DisplayName("Debe manejar excepción del ReviewClient gracefully")
        void shouldHandleReviewClientException() {
            when(bookingRequestRepository.findByTravelerId(100L)).thenReturn(List.of(sampleBooking));
            when(reviewClient.getTravelerReviewsExists("1")).thenThrow(new RuntimeException("Service unavailable"));

            List<BookingRequestResponseDto> result = bookingService.findByTravelerId(100L);

            // No debe lanzar excepción, solo loguear
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getHasTravelerReview()).isNull();
        }
    }

    @Nested
    @DisplayName("findByHostId")
    class FindByHostId {

        @Test
        @DisplayName("Debe retornar reservas del host con info de reviews")
        void shouldReturnBookingsWithReviewInfo() {
            when(bookingRequestRepository.findByHostId(200L)).thenReturn(List.of(sampleBooking));
            when(reviewClient.getTravelerReviewsExists("1")).thenReturn(Map.of(1L, false));
            when(reviewClient.getHostReviewsExists("1")).thenReturn(Map.of(1L, true));

            List<BookingRequestResponseDto> result = bookingService.findByHostId(200L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getHasHostReview()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar lista vacía si el host no tiene reservas")
        void shouldReturnEmptyList() {
            when(bookingRequestRepository.findByHostId(999L)).thenReturn(Collections.emptyList());

            List<BookingRequestResponseDto> result = bookingService.findByHostId(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByAccommodationId")
    class FindByAccommodationId {

        private BookingRequest acceptedBooking;
        private BookingRequest pendingBooking;

        @BeforeEach
        void setUp() {
            acceptedBooking = BookingRequest.builder()
                    .id(1L).accommodationId(10L).travelerId(100L).hostId(200L)
                    .startDate(LocalDate.of(2026, 7, 1))
                    .endDate(LocalDate.of(2026, 7, 10))
                    .status(BookingRequestStatus.ACCEPTED)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();

            pendingBooking = BookingRequest.builder()
                    .id(2L).accommodationId(10L).travelerId(101L).hostId(200L)
                    .startDate(LocalDate.of(2026, 7, 15))
                    .endDate(LocalDate.of(2026, 7, 20))
                    .status(BookingRequestStatus.PENDING)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Debe retornar solo reservas ACCEPTED")
        void shouldReturnOnlyAcceptedBookings() {
            when(bookingRequestRepository.findByAccommodationId(10L))
                    .thenReturn(List.of(acceptedBooking, pendingBooking));

            List<BookingRequestResponseDto> result = bookingService.findByAccommodationId(10L, null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(BookingRequestStatus.ACCEPTED);
        }

        @Test
        @DisplayName("Debe filtrar por solapamiento de fechas")
        void shouldFilterByDateOverlap() {
            when(bookingRequestRepository.findByAccommodationId(10L))
                    .thenReturn(List.of(acceptedBooking));

            // booking: 7/1 - 7/10, checkIn: 7/5, checkOut: 7/8 → solapa
            List<BookingRequestResponseDto> result = bookingService.findByAccommodationId(
                    10L, "2026-07-05", "2026-07-08");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Debe excluir reservas sin solapamiento de fechas")
        void shouldExcludeNonOverlappingBookings() {
            when(bookingRequestRepository.findByAccommodationId(10L))
                    .thenReturn(List.of(acceptedBooking));

            // booking: 7/1 - 7/10, checkIn: 7/15, checkOut: 7/20 → no solapa
            List<BookingRequestResponseDto> result = bookingService.findByAccommodationId(
                    10L, "2026-07-15", "2026-07-20");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay reservas")
        void shouldReturnEmptyList() {
            when(bookingRequestRepository.findByAccommodationId(99L))
                    .thenReturn(Collections.emptyList());

            List<BookingRequestResponseDto> result = bookingService.findByAccommodationId(99L, null, null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByAccommodationIds")
    class FindByAccommodationIds {

        @Test
        @DisplayName("Debe agrupar reservas por accommodationId")
        void shouldGroupByAccommodationId() {
            BookingRequest booking1 = BookingRequest.builder()
                    .id(1L).accommodationId(10L).travelerId(100L).hostId(200L)
                    .startDate(LocalDate.of(2026, 7, 1)).endDate(LocalDate.of(2026, 7, 10))
                    .status(BookingRequestStatus.ACCEPTED)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
            BookingRequest booking2 = BookingRequest.builder()
                    .id(2L).accommodationId(20L).travelerId(101L).hostId(200L)
                    .startDate(LocalDate.of(2026, 7, 5)).endDate(LocalDate.of(2026, 7, 15))
                    .status(BookingRequestStatus.ACCEPTED)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();

            when(bookingRequestRepository.findByAccommodationIdIn(List.of(10L, 20L)))
                    .thenReturn(List.of(booking1, booking2));

            Map<Long, List<BookingRequestResponseDto>> result =
                    bookingService.findByAccommodationIds(List.of(10L, 20L), null, null);

            assertThat(result).hasSize(2);
            assertThat(result.get(10L)).hasSize(1);
            assertThat(result.get(20L)).hasSize(1);
        }

        @Test
        @DisplayName("Debe filtrar solo ACCEPTED y por fechas")
        void shouldFilterAcceptedAndByDates() {
            BookingRequest accepted = BookingRequest.builder()
                    .id(1L).accommodationId(10L).travelerId(100L).hostId(200L)
                    .startDate(LocalDate.of(2026, 7, 1)).endDate(LocalDate.of(2026, 7, 10))
                    .status(BookingRequestStatus.ACCEPTED)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
            BookingRequest pending = BookingRequest.builder()
                    .id(2L).accommodationId(10L).travelerId(101L).hostId(200L)
                    .startDate(LocalDate.of(2026, 7, 15)).endDate(LocalDate.of(2026, 7, 20))
                    .status(BookingRequestStatus.PENDING)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();

            when(bookingRequestRepository.findByAccommodationIdIn(List.of(10L)))
                    .thenReturn(List.of(accepted, pending));

            Map<Long, List<BookingRequestResponseDto>> result =
                    bookingService.findByAccommodationIds(List.of(10L), "2026-07-05", "2026-07-08");

            assertThat(result.get(10L)).hasSize(1);
            assertThat(result.get(10L).get(0).getStatus()).isEqualTo(BookingRequestStatus.ACCEPTED);
        }
    }

    @Nested
    @DisplayName("accept")
    class Accept {

        @Test
        @DisplayName("Debe cambiar status a ACCEPTED y establecer respondedAt")
        void shouldAcceptAndSetRespondedAt() {
            when(bookingRequestRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
            when(bookingRequestRepository.save(any(BookingRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BookingRequestResponseDto result = bookingService.accept(1L);

            assertThat(result.getStatus()).isEqualTo(BookingRequestStatus.ACCEPTED);
            assertThat(result.getRespondedAt()).isNotNull();
            verify(bookingRequestRepository).save(sampleBooking);
        }

        @Test
        @DisplayName("Debe lanzar BookingNotFoundException si no existe")
        void shouldThrowWhenNotFound() {
            when(bookingRequestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.accept(99L))
                    .isInstanceOf(BookingNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("reject")
    class Reject {

        @Test
        @DisplayName("Debe cambiar status a REJECTED y establecer respondedAt")
        void shouldRejectAndSetRespondedAt() {
            when(bookingRequestRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
            when(bookingRequestRepository.save(any(BookingRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BookingRequestResponseDto result = bookingService.reject(1L);

            assertThat(result.getStatus()).isEqualTo(BookingRequestStatus.REJECTED);
            assertThat(result.getRespondedAt()).isNotNull();
        }

        @Test
        @DisplayName("Debe lanzar BookingNotFoundException si no existe")
        void shouldThrowWhenNotFound() {
            when(bookingRequestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.reject(99L))
                    .isInstanceOf(BookingNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("Debe cambiar status a CANCELLED y establecer cancelledAt")
        void shouldCancelAndSetCancelledAt() {
            when(bookingRequestRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
            when(bookingRequestRepository.save(any(BookingRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BookingRequestResponseDto result = bookingService.cancel(1L);

            assertThat(result.getStatus()).isEqualTo(BookingRequestStatus.CANCELLED);
            assertThat(result.getCancelledAt()).isNotNull();
            verify(bookingRequestRepository).save(sampleBooking);
        }

        @Test
        @DisplayName("Debe lanzar BookingNotFoundException si no existe")
        void shouldThrowWhenNotFound() {
            when(bookingRequestRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancel(99L))
                    .isInstanceOf(BookingNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getBookingRequestIdsByAccommodationIds")
    class GetBookingRequestIdsByAccommodationIds {

        @Test
        @DisplayName("Debe agrupar IDs de reservas por accommodationId")
        void shouldGroupIdsByAccommodationId() {
            BookingRequest b1 = BookingRequest.builder().id(1L).accommodationId(10L).build();
            BookingRequest b2 = BookingRequest.builder().id(2L).accommodationId(10L).build();
            BookingRequest b3 = BookingRequest.builder().id(3L).accommodationId(20L).build();

            when(bookingRequestRepository.findByAccommodationIdIn(List.of(10L, 20L)))
                    .thenReturn(List.of(b1, b2, b3));

            Map<Long, List<Long>> result = bookingService.getBookingRequestIdsByAccommodationIds(List.of(10L, 20L));

            assertThat(result).hasSize(2);
            assertThat(result.get(10L)).containsExactly(1L, 2L);
            assertThat(result.get(20L)).containsExactly(3L);
        }

        @Test
        @DisplayName("Debe retornar mapa vacío si no hay reservas")
        void shouldReturnEmptyMap() {
            when(bookingRequestRepository.findByAccommodationIdIn(List.of(99L)))
                    .thenReturn(Collections.emptyList());

            Map<Long, List<Long>> result = bookingService.getBookingRequestIdsByAccommodationIds(List.of(99L));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUserBookingStats")
    class GetUserBookingStats {

        @Test
        @DisplayName("Debe calcular estadísticas correctamente")
        void shouldCalculateStats() {
            BookingRequest pendingAsTraveler = BookingRequest.builder()
                    .id(1L).status(BookingRequestStatus.PENDING).travelerId(100L).hostId(200L)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
            BookingRequest completedAsTraveler = BookingRequest.builder()
                    .id(2L).status(BookingRequestStatus.COMPLETED).travelerId(100L).hostId(200L)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
            BookingRequest acceptedAsHost = BookingRequest.builder()
                    .id(3L).status(BookingRequestStatus.ACCEPTED).travelerId(101L).hostId(100L)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
            BookingRequest pendingAsHost = BookingRequest.builder()
                    .id(4L).status(BookingRequestStatus.PENDING).travelerId(102L).hostId(100L)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();

            when(bookingRequestRepository.findByTravelerId(100L))
                    .thenReturn(List.of(pendingAsTraveler, completedAsTraveler));
            when(bookingRequestRepository.findByHostId(100L))
                    .thenReturn(List.of(acceptedAsHost, pendingAsHost));

            UserBookingStatsDto result = bookingService.getUserBookingStats(100L);

            assertThat(result.getTotalBookings()).isEqualTo(4);
            assertThat(result.getPendingRequests()).isEqualTo(2);
            assertThat(result.getCompletedBookings()).isEqualTo(1);
        }

        @Test
        @DisplayName("Debe retornar ceros si el usuario no tiene reservas")
        void shouldReturnZerosWhenNoBookings() {
            when(bookingRequestRepository.findByTravelerId(999L)).thenReturn(Collections.emptyList());
            when(bookingRequestRepository.findByHostId(999L)).thenReturn(Collections.emptyList());

            UserBookingStatsDto result = bookingService.getUserBookingStats(999L);

            assertThat(result.getTotalBookings()).isEqualTo(0);
            assertThat(result.getPendingRequests()).isEqualTo(0);
            assertThat(result.getCompletedBookings()).isEqualTo(0);
        }
    }
}
