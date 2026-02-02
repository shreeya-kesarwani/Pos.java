package com.pos.scheduler;

import com.pos.api.DaySalesApi;
import com.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DaySalesScheduler {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    private DaySalesApi daySalesApi;

    // Use cron in production; fixedDelay is fine for testing.
    // Example cron (11:03 AM IST): 0 3 11 * * *
    // @Scheduled(cron = "0 3 11 * * *", zone = "Asia/Kolkata")

    @Scheduled(fixedDelay = 10000)
    public void computeYesterday() throws ApiException {
//todo - scheduler should not handle this logic, it should only call the method
        // Yesterday in BUSINESS timezone, at start-of-day boundary
        ZonedDateTime yesterdayStartBusiness =
                ZonedDateTime.now(BUSINESS_ZONE)
                        .minusDays(1)
                        .toLocalDate()
                        .atStartOfDay(BUSINESS_ZONE);

        daySalesApi.calculateAndStore(yesterdayStartBusiness);
    }
}
