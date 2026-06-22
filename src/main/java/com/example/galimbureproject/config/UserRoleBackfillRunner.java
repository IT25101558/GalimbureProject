package com.example.galimbureproject.config;

import com.example.galimbureproject.service.UserRoleBackfillService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserRoleBackfillRunner implements ApplicationRunner {

    private final UserRoleBackfillService userRoleBackfillService;

    public UserRoleBackfillRunner(UserRoleBackfillService userRoleBackfillService) {
        this.userRoleBackfillService = userRoleBackfillService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRoleBackfillService.backfillMissingRoles();
    }
}
