package com.ltm.paypilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class PayPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayPilotApplication.class, args);
    }

}
