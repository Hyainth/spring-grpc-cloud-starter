package com.github.hyacinth.grpc.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hyacinth.grpc.discovery")
public class DiscoveryResolverProperties {

    /**
     * Whether the discovery resolver is enabled.
     */
    private boolean enabled = true;

    /**
     * Load-balancing policy applied only to discovery-based channels.
     */
    private String loadBalancingPolicy = "round_robin";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLoadBalancingPolicy() {
        return loadBalancingPolicy;
    }

    public void setLoadBalancingPolicy(String loadBalancingPolicy) {
        this.loadBalancingPolicy = loadBalancingPolicy;
    }
}
