package com.home.pubsubtest;

import reactor.core.publisher.Flux;

public class ReactorEx {
    public static void main(String[] args) {

        Flux.<Integer>create(e -> {
            e.next(1);   //subscription 안에 있던 코드를 람다식으로 구현 가능
            e.next(2);
            e.next(3);
            e.complete();
        })
        .log() //위 단계에 해당하는 로그를 찍어준다
        .map(s->s*10)
        .reduce(0, (a,b)-> a+b)
        .log()
        .subscribe(System.out::println);  //onNext 에 해당하는 로직만 람다식으로 구현 가능

    }
}
