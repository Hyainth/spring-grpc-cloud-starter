package com.github.hyacinth.grpc.nacos.autoconfigure;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({NacosRegistration.class, NacosRegistrationCustomizer.class})
@ConditionalOnBean(NacosRegistration.class)
@ConditionalOnProperty(prefix = "spring.grpc.server", name = "port")
public class GrpcNacosDiscoveryAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GrpcNacosDiscoveryAutoConfiguration.class);

    private static final String GRPC_PORT_METADATA_KEY = "gRPC_port";

    @Bean
    @ConditionalOnMissingBean(name = "grpcPortMetadataRegistrationCustomizer")
    NacosRegistrationCustomizer grpcPortMetadataRegistrationCustomizer(
            @Value("${spring.grpc.server.port}") int grpcPort) {
        return registration -> {
            registration.getMetadata().put(GRPC_PORT_METADATA_KEY, String.valueOf(grpcPort));
            log.info("Published Nacos metadata {}={}.", GRPC_PORT_METADATA_KEY, grpcPort);
        };
    }
}
