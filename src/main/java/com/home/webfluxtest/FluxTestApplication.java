package com.home.webfluxtest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@RestController
@Slf4j
public class FluxTestApplication {

    @GetMapping("/event/{id}")
    Mono<List<Event>> event(@PathVariable long id) {

        List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
        return Mono.just(list);
    }

    @GetMapping(value="/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> events() {

        //return Flux.just(new Event(1L, "event1"), new Event(2L, "event2"));

        //List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
        //return Flux.fromIterable(list); //map, filter 등 사용가능


        /*
        return Flux
                //.fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), "value"))) //무한히 생성되는 스트림
                //.<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
                .<Event, Long>generate(() -> 1L, (id, sink) -> {
                    sink.next(new Event(id, "value" + id));
                    return id + 1;
                })
                .delayElements(Duration.ofSeconds(1))
                .take(10);

         */


        /*
        Flux<Event> es = Flux.<Event, Long>generate(() -> 1L, (id, sink) -> {
            sink.next(new Event(id, "value" + id));
            return id + 1;
        });

        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

        return Flux.zip(es, interval).map(tu -> tu.getT1());

        */

        Flux<String> es = Flux.generate(sink -> sink.next("Value"));
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

        return Flux.zip(es, interval).map(tu -> new Event(tu.getT2(), tu.getT1())).take(10);
    }

    private String generateHello() {
        log.info("method generateHello();");
        return "Hello Mono";
    }

    @Data
    @AllArgsConstructor
    public static class Event {

        long id;
        String value;
    }

    public static void main(String[] args) {
        //System.setProperty("reactor.ipc.netty.workerCount", "1");
        //System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
        SpringApplication.run(FluxTestApplication.class, args);
    }
}
