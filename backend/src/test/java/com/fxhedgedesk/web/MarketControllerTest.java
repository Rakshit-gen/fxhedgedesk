package com.fxhedgedesk.web;

import com.fxhedgedesk.domain.CurrencyPair;
import com.fxhedgedesk.domain.FxRateTick;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.repository.FxRateTickRepository;
import com.fxhedgedesk.security.JwtService;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import com.fxhedgedesk.service.simulation.SimulationClockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MarketController.class)
@AutoConfigureMockMvc(addFilters = false)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FxRateSimulationService rateService;

    @MockBean
    private FxRateTickRepository fxRateTickRepository;

    @MockBean
    private SimulationClockService clockService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    private final CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.08"),
            new BigDecimal("0.08"), BigDecimal.ZERO);

    @Test
    void pairsCombinesEachPairWithItsLiveRate() throws Exception {
        when(rateService.pairs()).thenReturn(List.of(eurUsd));
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.0950"));

        mockMvc.perform(get("/api/market/pairs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("EURUSD"))
                .andExpect(jsonPath("$[0].currentRate").value(1.0950));
    }

    @Test
    void historyIsReturnedOldestFirstEvenThoughTheRepositoryFetchesNewestFirst() throws Exception {
        FxRateTick day5 = new FxRateTick("EURUSD", new BigDecimal("1.09"), 5);
        FxRateTick day3 = new FxRateTick("EURUSD", new BigDecimal("1.07"), 3);
        FxRateTick day1 = new FxRateTick("EURUSD", new BigDecimal("1.08"), 1);
        when(fxRateTickRepository.findByPairCodeOrderBySimDayDescCreatedAtDesc(any(), any()))
                .thenReturn(List.of(day5, day3, day1));

        mockMvc.perform(get("/api/market/pairs/EURUSD/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].simDay").value(1))
                .andExpect(jsonPath("$[1].simDay").value(3))
                .andExpect(jsonPath("$[2].simDay").value(5));
    }

    @Test
    void clockReportsTheCurrentSimulatedDay() throws Exception {
        when(clockService.currentSimDay()).thenReturn(42L);

        mockMvc.perform(get("/api/market/clock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSimDay").value(42));
    }
}
