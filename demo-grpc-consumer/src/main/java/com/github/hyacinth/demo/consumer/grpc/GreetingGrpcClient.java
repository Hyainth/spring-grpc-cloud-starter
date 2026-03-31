package com.github.hyacinth.demo.consumer.grpc;

import com.github.hyacinth.proto.greeting.GreetingReply;
import com.github.hyacinth.proto.greeting.GreetingRequest;
import com.github.hyacinth.proto.greeting.GreetingServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GreetingGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(GreetingGrpcClient.class);

    private final GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub;

    public GreetingGrpcClient(GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public GreetingCallResult sayHello(String name, String source) {
        GreetingRequest request = GreetingRequest.newBuilder()
                .setName(name)
                .setSource(source)
                .build();

        log.info("consumer 发起 gRPC 调用: name={}, source={}", name, source);
        GreetingReply reply = blockingStub.sayHello(request);
        log.info("consumer gRPC 调用成功: message={}", reply.getMessage());

        return new GreetingCallResult(reply.getMessage(), source);
    }

    public record GreetingCallResult(String message, String source) {
    }
}
