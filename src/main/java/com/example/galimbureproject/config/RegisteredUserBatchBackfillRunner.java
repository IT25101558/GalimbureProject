package com.example.galimbureproject.config;

import com.example.galimbureproject.service.RegisteredUserBatchBackfillService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RegisteredUserBatchBackfillRunner implements ApplicationRunner {

    private final RegisteredUserBatchBackfillService registeredUserBatchBackfillService;

    public RegisteredUserBatchBackfillRunner(RegisteredUserBatchBackfillService registeredUserBatchBackfillService) {
        this.registeredUserBatchBackfillService = registeredUserBatchBackfillService;
    }

    @Override
    public void run(ApplicationArguments args) {
        registeredUserBatchBackfillService.backfillMissingBatchYears();
    }
}
