package com.euni.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "app.logging")
@Getter
@Setter
public class LoggingProperties {

    private List<String> excludedUrls = new ArrayList<>();
    private Set<String> maskedFields = new HashSet<>();
    private int maxFieldLength = 500;
}
