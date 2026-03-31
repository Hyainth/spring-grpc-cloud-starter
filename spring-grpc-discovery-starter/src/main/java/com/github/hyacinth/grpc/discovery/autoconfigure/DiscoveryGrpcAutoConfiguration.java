package com.github.hyacinth.grpc.discovery.autoconfigure;

import com.github.hyacinth.grpc.discovery.DiscoveryNameResolverProvider;
import com.github.hyacinth.grpc.discovery.DiscoveryNameResolverRefresher;
import com.github.hyacinth.grpc.discovery.DiscoveryNameResolverRegistrar;
import com.github.hyacinth.grpc.discovery.DiscoveryResolverProperties;
import io.grpc.NameResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({NameResolver.class, DiscoveryClient.class})
@ConditionalOnBean(DiscoveryClient.class)
@ConditionalOnProperty(prefix = "hyacinth.grpc.discovery", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DiscoveryResolverProperties.class)
public class DiscoveryGrpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DiscoveryNameResolverProvider discoveryNameResolverProvider(
            DiscoveryClient discoveryClient,
            DiscoveryResolverProperties properties
    ) {
        return new DiscoveryNameResolverProvider(discoveryClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    DiscoveryNameResolverRegistrar discoveryNameResolverRegistrar(DiscoveryNameResolverProvider provider) {
        return new DiscoveryNameResolverRegistrar(provider);
    }

    @Bean
    @ConditionalOnMissingBean
    DiscoveryNameResolverRefresher discoveryNameResolverRefresher(DiscoveryNameResolverProvider provider) {
        return new DiscoveryNameResolverRefresher(provider);
    }
}
