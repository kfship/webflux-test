package com.home.webfluxtest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@Slf4j
public class MonoTestApplication {

    @GetMapping("/")
    Mono<String> hello() {


// 아래코드와  달리 pos1, pos2가 실행되고 generateHello()는 subscribe가 시작될 때 실행된다
        log.info("pos1");
        Mono<String> m = Mono.fromSupplier(() -> generateHello()).doOnNext(c -> log.info(c));  // Publisher -> (Publisher) -> (Publisher) -> Subscriber

        //m.subscribe(); //중간에 한번 subscribe 하면 이 때 Mono체인을 실행하고 나중에 스프링이 호출할 때 또 실행한다

        String msg2 = m.block();  //subscribe 할 때와 동일하게 Mono체인을 먼저 실행하고 나중에 스프링이 호출할 때 또 실행한다

        log.info("pos2 : " + msg2);
        //return Mono.just(msg2); //m을 리턴하면 Mono체인이 모두 다시 실행되기 때문에 msg2만 리턴하게 할 수 있다
        return m;

// pos1, "Hello Mono", pos2 순서로 실행된다. generateHello()는 동기식으로 호출되는 것임
// just는 이미 만들어진 값을 반환할 때 사용한다
//        log.info("pos1");
//        Mono m = Mono.just(generateHello()).log();  // Publisher -> (Publisher) -> (Publisher) -> Subscriber
//        log.info("pos2");
//        return m;
    }

    private String generateHello() {
        log.info("method generateHello();");
        return "Hello Mono";
    }

    public static void main(String[] args) {
        //System.setProperty("reactor.ipc.netty.workerCount", "1");
        //System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
        SpringApplication.run(MonoTestApplication.class, args);
    }
}
