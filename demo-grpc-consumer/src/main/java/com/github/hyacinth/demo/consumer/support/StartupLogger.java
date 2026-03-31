package com.github.hyacinth.demo.consumer.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final Registration registration;

    @Value("${spring.grpc.client.channels.provider.address}")
    private String providerTarget;

    public StartupLogger(Registration registration) {
        this.registration = registration;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Consumer 启动完成: serviceId={}, host={}, port={}, providerTarget={}, metadata={}",
                registration.getServiceId(),
                registration.getHost(),
                registration.getPort(),
                providerTarget,
                registration.getMetadata());
    }
}
