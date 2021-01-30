package com.home.webfluxtest;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@SpringBootApplication
@RestController
@Slf4j
@EnableAsync
public class WebfluxTestApplication {

    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    @Autowired
    MyService myService;

    WebClient client = WebClient.create();

    @GetMapping("/rest")
    public Mono<String> rest(int idx) {

        //정의하는것만으로는 API 호출이 되지 않는다
        //publisher는 subscriber가 subscribe하지 않으면 데이터를 만들지 않는다
        //스프링 컨테이너가 return 타입이 Mono형태이면 알아서 호출한다
        Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange();

        //clientRresponse 를 Mono<String> 으로 변환한다
        //Mono<Mono<String>> body = res.map(clientResponse -> clientResponse.bodyToMono(String.class));

        return res                                                              //Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class))                       //Mono<String>
                .doOnNext(c -> log.info(c.toString()))
                .flatMap(res1 -> client.get().uri(URL2, res1).exchange())       //Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class))                       //Mono<String>
                .doOnNext(c -> log.info(c.toString()))
                .flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2)))
                .doOnNext(c -> log.info(c.toString()));

        /*
        ClientResponse cr = null;
        Mono<String> body =   cr.bodyToMono(String.class);
        */
    }

    public static void main(String[] args) {
        System.setProperty("reactor.ipc.netty.workerCount", "1");
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
        SpringApplication.run(WebfluxTestApplication.class, args);
    }


    @Service
    public static class MyService {
        @Async
        public CompletableFuture<String> work(String req) {

            try { Thread.sleep(1000); } catch(Exception ex) { ex.printStackTrace();}
            return CompletableFuture.completedFuture(req + "/asyncwork");
        }
    }

    @RequestMapping("/hello")
    public Publisher<String> hello(String name) {
        return new Publisher<String>() {
            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long l) {
                        subscriber.onNext("hello " + name);
                        subscriber.onComplete();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}

