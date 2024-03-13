package com.example.jobapi.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.example.jobapi.dto.JobDto;
import com.example.jobapi.enums.ErrorResponseCode;
import com.example.jobapi.enums.SuccessResponseCode;
import com.example.jobapi.model.Job;
import com.example.jobapi.util.CommonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import reactor.core.publisher.Flux;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class TestController extends BaseController {

    @GetMapping("/test001")
    public ResponseEntity<?> response(
            @RequestParam(name = "salary", required = false) String salary) throws JsonProcessingException {
        return ResponseEntity.ok().body("resultList");
    }

    @RequestMapping(value = "/resource-uri", method = RequestMethod.GET)
    public SseEmitter handle() {
        SseEmitter emitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Creating a new thread for async processing
        executor.execute(() -> {
            try {
                UUID uuid = UUID.randomUUID();
                emitter.send("dataSet:" + uuid);
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        executor.shutdown();

        return emitter;
    }

    @GetMapping(path = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux - " + LocalTime.now().toString());
    }

    @PostMapping("/CreateOrder")
    public String createOrder(@RequestBody String entity) {
        // TODO: process POST request
        streamSseMvc(entity);
        return entity;
    }

    @GetMapping(path = "/update-order")
    public Flux<ServerSentEvent<String>> updateOrder(String result) {

        return Flux.just("asfadsfdfasf").map(sequence -> ServerSentEvent.<String>builder()
                .id(String.valueOf(sequence))
                .event("periodic-event")
                .data("SSE - " + LocalTime.now().toString())
                .build());

        // ServerSentEvent<String> temp = ServerSentEvent.<String>builder()
        // .id(String.valueOf(result))
        // .event("periodic-event")
        // .data("SSE - " + LocalTime.now().toString())
        // .build();
        // return temp;
    }

    @GetMapping("/stream-sse")
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(sequence))
                        .event("periodic-event")
                        .data("SSE - " + LocalTime.now().toString())
                        .build());
    }

    @GetMapping("/stream-sse-mvc")
    public SseEmitter streamSseMvc(String Msg) {
        SseEmitter emitter = new SseEmitter();
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> {
            try {
                UUID uuid = UUID.randomUUID();
                SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + LocalTime.now().toString() + Msg)
                            .id(String.valueOf(uuid))
                            .name("sse event - mvc");
                    emitter.send(event);
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }
    //Order

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String orderStatus = "Default Status";

    @GetMapping("/status")
    public String showOrderStatus() {
        return "order-status";
    }

    @GetMapping("/status/sse")
    @ResponseBody
    public String orderStatusSSE() {
        return "data: " + orderStatus + "\n\n";
    }

    private final SseEmitter emitter = new SseEmitter();
    @PostMapping("/update")
    @ResponseBody
    public String updateOrderStatus(@RequestBody String entity) {
        executorService.submit(() -> {
            // Simulate order processing
            //orderStatusService.updateOrderStatus(order);
            updateOrderStatusService(entity);

            // Notify clients about the updated order status using SSE
            //orderStatusService.sendOrderStatusSSE(order);
            sendOrderStatusSSE(entity);
        });

        return "Order status update initiated!";
    }
    public void updateOrderStatusService(String order) {
        // Simulate order processing
        orderStatus = order;
        // Additional processing logic...
    }

    public void sendOrderStatusSSE(String order) {
        try {
            // Send the updated order status to all connected clients
            emitter.send(SseEmitter.event().data(order));
        } catch (IOException e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    public SseEmitter getEmitter() {
        return emitter;
    }
}
