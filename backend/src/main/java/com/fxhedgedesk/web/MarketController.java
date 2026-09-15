package com.fxhedgedesk.web;

import com.fxhedgedesk.domain.CurrencyPair;
import com.fxhedgedesk.domain.FxRateTick;
import com.fxhedgedesk.repository.FxRateTickRepository;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import com.fxhedgedesk.service.simulation.SimulationClockService;
import com.fxhedgedesk.web.dto.MarketDtos.ClockResponse;
import com.fxhedgedesk.web.dto.MarketDtos.CurrencyPairResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final FxRateSimulationService rateService;
    private final FxRateTickRepository fxRateTickRepository;
    private final SimulationClockService clockService;

    public MarketController(FxRateSimulationService rateService, FxRateTickRepository fxRateTickRepository,
                             SimulationClockService clockService) {
        this.rateService = rateService;
        this.fxRateTickRepository = fxRateTickRepository;
        this.clockService = clockService;
    }

    @GetMapping("/pairs")
    public List<CurrencyPairResponse> pairs() {
        return rateService.pairs().stream()
                .map(pair -> CurrencyPairResponse.from(pair, rateService.getCurrentRate(pair.getCode())))
                .toList();
    }

    @GetMapping("/pairs/{code}/history")
    public List<FxRateTick> history(@PathVariable String code) {
        List<FxRateTick> ticks = fxRateTickRepository.findByPairCodeOrderBySimDayDescCreatedAtDesc(code, PageRequest.of(0, 120));
        return ticks.stream().sorted(Comparator.comparingLong(FxRateTick::getSimDay)).toList();
    }

    @GetMapping("/clock")
    public ClockResponse clock() {
        return new ClockResponse(clockService.currentSimDay());
    }
}
