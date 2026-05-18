package com.shareyourtrip.microservice.bookings.ShareYourTripBookingsMs.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            String userId = request.getHeader("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                template.header("X-User-Id", userId);
            }
            
            // Las llamadas Feign entre microservicios siempre deben tener X-Gateway-Request
            template.header("X-Gateway-Request", "true");
        }
    }
}
