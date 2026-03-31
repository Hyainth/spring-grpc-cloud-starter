package com.github.hyacinth.grpc.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.event.EventListener;

public class DiscoveryNameResolverRefresher {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryNameResolverRefresher.class);

    private final DiscoveryNameResolverProvider provider;

    public DiscoveryNameResolverRefresher(DiscoveryNameResolverProvider provider) {
        this.provider = provider;
    }

    @EventListener(HeartbeatEvent.class)
    public void onHeartbeat(HeartbeatEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Received HeartbeatEvent, refreshing discovery resolvers. value={}", event.getValue());
        }
        provider.refreshAll("heartbeat-event");
    }
}
