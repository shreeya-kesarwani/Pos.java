package com.pos.scheduler;

import com.pos.api.DaySalesApi;
import com.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DaySalesScheduler {

    @Autowired
    private DaySalesApi daySalesApi;

//    @Scheduled(cron = "0 03 11 * * ?", zone = "Asia/Kolkata")
    @Scheduled(fixedDelay = 10000)
    public void computeYesterday() throws ApiException {
        LocalDate yesterday = LocalDate.now();
        daySalesApi.calculateAndStore(yesterday);
    }
}
