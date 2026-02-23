package com.pos.scheduler;

import com.pos.api.DaySalesApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DaySalesScheduler {

    private static final Logger log = LoggerFactory.getLogger(DaySalesScheduler.class);

    @Autowired
    DaySalesApi daySalesApi;

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Kolkata")
    //@Scheduled(cron = "0 3 11 * * *", zone = "Asia/Kolkata")
    public void compute() {
        try {
            daySalesApi.calculateDaySales();
            log.info("DaySalesScheduler finished successfully");
        } catch (Exception e) {
            log.error("Unexpected error in DaySalesScheduler", e);
            log.error("DaySalesScheduler failed");
        }
    }
}