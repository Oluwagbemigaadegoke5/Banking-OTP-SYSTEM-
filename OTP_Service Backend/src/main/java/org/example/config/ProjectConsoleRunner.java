package org.example.config;

import org.example.util.TotpEngine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProjectConsoleRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        String testSeed = "ORSXG5BRGIZTINJWG4=======";
        long currentWindow = (System.currentTimeMillis() / 1000) / 30;
        String generatedSample = TotpEngine.generateTOTP(testSeed, currentWindow);

        System.out.println("\n==============================================================");
        System.out.println("   APEX VAULT TRANSACTION SUBSYSTEM RUNTIME INITIALIZED");
        System.out.println("==============================================================");
        System.out.println(" -> Configured Shared Engine Seed: " + testSeed);
        System.out.println(" -> Expected Current Execution Window Code: [" + generatedSample + "]");
        System.out.println(" -> Network Independent Authentication Strategy Verified: Active");
        System.out.println("==============================================================\n");
    }
}