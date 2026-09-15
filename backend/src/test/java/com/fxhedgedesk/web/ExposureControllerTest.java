package com.fxhedgedesk.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.CurrencyPair;
import com.fxhedgedesk.domain.Exposure;
import com.fxhedgedesk.domain.ExposureDirection;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.security.JwtService;
import com.fxhedgedesk.service.ExposureService;
import com.fxhedgedesk.web.dto.ExposureDtos.BookExposureRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExposureController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExposureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExposureService exposureService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");
    private final CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.08"),
            new BigDecimal("0.08"), BigDecimal.ZERO);

    private RequestPostProcessor asUser() {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test
    void bookingWithABlankPairCodeIsRejectedBeforeReachingTheService() throws Exception {
        String badJson = """
                {"pairCode":"","direction":"RECEIVABLE","amount":1000,"daysUntilDue":30,"description":"Invoice"}""";

        mockMvc.perform(post("/api/exposures")
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookingWithADueDayOfZeroIsRejectedBeforeReachingTheService() throws Exception {
        String badJson = """
                {"pairCode":"EURUSD","direction":"RECEIVABLE","amount":1000,"daysUntilDue":0,"description":"Invoice"}""";

        mockMvc.perform(post("/api/exposures")
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookingAValidExposureReturnsItsSerializedForm() throws Exception {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("8000"),
                new BigDecimal("1.0950"), 15, "Test invoice");
        when(exposureService.bookExposure(any(), eq("EURUSD"), eq(ExposureDirection.RECEIVABLE),
                eq(new BigDecimal("8000")), eq(15L), eq("Test invoice"))).thenReturn(exposure);

        BookExposureRequest request = new BookExposureRequest("EURUSD", ExposureDirection.RECEIVABLE,
                new BigDecimal("8000"), 15, "Test invoice");

        mockMvc.perform(post("/api/exposures")
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pairCode").value("EURUSD"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void listingReturnsEveryExposureForTheAuthenticatedUser() throws Exception {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.PAYABLE, new BigDecimal("3000"),
                new BigDecimal("1.08"), 10, "Bill");
        when(exposureService.listForUser(any())).thenReturn(List.of(exposure));

        mockMvc.perform(get("/api/exposures").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].direction").value("PAYABLE"));
    }
}
