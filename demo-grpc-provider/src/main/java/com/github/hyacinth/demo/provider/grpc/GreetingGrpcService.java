package com.github.hyacinth.demo.provider.grpc;

import com.github.hyacinth.proto.greeting.GreetingReply;
import com.github.hyacinth.proto.greeting.GreetingRequest;
import com.github.hyacinth.proto.greeting.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class GreetingGrpcService extends GreetingServiceGrpc.GreetingServiceImplBase {
    @Override
    public void sayHello(GreetingRequest request, StreamObserver<GreetingReply> responseObserver) {
        GreetingReply reply = GreetingReply.newBuilder()
                .setMessage("hello")
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
