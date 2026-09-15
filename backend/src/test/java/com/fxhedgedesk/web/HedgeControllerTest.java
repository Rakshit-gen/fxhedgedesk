package com.fxhedgedesk.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.exception.NotFoundException;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.security.JwtService;
import com.fxhedgedesk.service.HedgeService;
import com.fxhedgedesk.web.dto.HedgeDtos.BookForwardRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HedgeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HedgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HedgeService hedgeService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");
    private final CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.08"),
            new BigDecimal("0.08"), BigDecimal.ZERO);

    private org.springframework.test.web.servlet.request.RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test
    void bookingWithANonPositiveNotionalIsRejectedBeforeReachingTheService() throws Exception {
        BookForwardRequest request = new BookForwardRequest(UUID.randomUUID(), BigDecimal.ZERO);

        mockMvc.perform(post("/api/forwards")
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookingAKnownExposureReturnsTheForwardWithItsUnrealizedPnl() throws Exception {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Invoice");
        ForwardContract forward = new ForwardContract(user, exposure, eurUsd, new BigDecimal("4000"),
                new BigDecimal("1.09"), ForwardDirection.SELL, 5, 30);
        when(hedgeService.bookForward(any(), any(), any())).thenReturn(forward);
        when(hedgeService.unrealizedPnl(forward)).thenReturn(new BigDecimal("12.50"));

        BookForwardRequest request = new BookForwardRequest(exposure.getId(), new BigDecimal("4000"));

        mockMvc.perform(post("/api/forwards")
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pairCode").value("EURUSD"))
                .andExpect(jsonPath("$.direction").value("SELL"))
                .andExpect(jsonPath("$.unrealizedPnl").value(12.50));
    }

    @Test
    void bookingAgainstAnExposureTheServiceCannotFindReturns404() throws Exception {
        when(hedgeService.bookForward(any(), any(), any())).thenThrow(new NotFoundException("Exposure not found"));

        BookForwardRequest request = new BookForwardRequest(UUID.randomUUID(), new BigDecimal("100"));

        mockMvc.perform(post("/api/forwards")
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listingReturnsEveryForwardTheServiceHasForTheUser() throws Exception {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.PAYABLE, new BigDecimal("5000"),
                new BigDecimal("1.08"), 20, "Bill");
        ForwardContract forward = new ForwardContract(user, exposure, eurUsd, new BigDecimal("2000"),
                new BigDecimal("1.08"), ForwardDirection.BUY, 2, 20);
        when(hedgeService.listForUser(any())).thenReturn(List.of(forward));
        when(hedgeService.unrealizedPnl(forward)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/api/forwards").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].direction").value("BUY"));
    }
}
