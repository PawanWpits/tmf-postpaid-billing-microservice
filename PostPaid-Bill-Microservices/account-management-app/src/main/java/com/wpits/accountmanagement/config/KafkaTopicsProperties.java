package com.wpits.accountmanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.kafka.topics")
public class KafkaTopicsProperties {
    private String billFormat;
    private String partyAccount;
    private String billingAccount;
    private String financialAccount;
    private String settlementAccount;
    private String billPresentationMedia;
    private String billingCycleSpecification;
}
