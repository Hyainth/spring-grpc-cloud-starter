package com.github.hyacinth.grpc.discovery;

import io.grpc.NameResolverRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class DiscoveryNameResolverRegistrar implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryNameResolverRegistrar.class);

    private final DiscoveryNameResolverProvider provider;

    public DiscoveryNameResolverRegistrar(DiscoveryNameResolverProvider provider) {
        this.provider = provider;
    }

    @Override
    public void afterPropertiesSet() {
        NameResolverRegistry.getDefaultRegistry().register(provider);
        log.info("Registered discovery NameResolverProvider for scheme '{}'.", GrpcDiscoveryConstants.DISCOVERY_SCHEME);
    }

    @Override
    public void destroy() {
        NameResolverRegistry.getDefaultRegistry().deregister(provider);
    }
}
