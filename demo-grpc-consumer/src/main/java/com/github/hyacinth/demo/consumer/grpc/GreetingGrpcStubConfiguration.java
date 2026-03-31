package com.github.hyacinth.demo.consumer.grpc;

import com.github.hyacinth.grpc.discovery.DiscoveryNameResolverRegistrar;
import com.github.hyacinth.proto.greeting.GreetingServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration(proxyBeanMethods = false)
public class GreetingGrpcStubConfiguration {

    @Bean
    GreetingServiceGrpc.GreetingServiceBlockingStub greetingServiceBlockingStub(
            GrpcChannelFactory channelFactory,
            DiscoveryNameResolverRegistrar registrar) {
        return GreetingServiceGrpc.newBlockingStub(channelFactory.createChannel("provider"));
    }
}
