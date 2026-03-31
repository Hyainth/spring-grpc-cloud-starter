package com.github.hyacinth.demo.consumer.controller;

import com.github.hyacinth.demo.consumer.grpc.GreetingGrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consumer")
public class ConsumerController {

    private final GreetingGrpcClient greetingGrpcClient;

    public ConsumerController(GreetingGrpcClient greetingGrpcClient) {
        this.greetingGrpcClient = greetingGrpcClient;
    }

    @GetMapping("/call")
    public GreetingGrpcClient.GreetingCallResult call(@RequestParam(defaultValue = "http-call") String name) {
        return greetingGrpcClient.sayHello(name, "consumer-http");
    }
}
