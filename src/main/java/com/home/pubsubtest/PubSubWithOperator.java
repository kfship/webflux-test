package com.home.pubsubtest;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Reactive Streams - Operators
 *
 * Publisher -> [Data1] -> Operator -> [Data2] -> Subscriber
 *    pub                   mapPub                 logSub
 *                          <- subscribe(logSub)
 *                          -> onSubscribe(s)
 *                          -> onNext
 *                          -> onNext
 *                          -> onComplete
 *
 * 1. map (d1 -> f -> d2)
 */
public class PubSubWithOperator {

    public static void main(String[] args) {


        Publisher<Integer> pub = interPub(Stream.iterate(1, a->a+1).limit(10).collect(Collectors.toList()));

        Publisher<Integer> mapPub = mapPub(pub, s -> s * 10);  //operator1
        //Publisher<Integer> map2Pub = mapPub(mapPub, (Function<Integer, Integer>)s -> -s);  //operator2
        //Publisher<Integer> sumPub = sumPub(pub);
        //Publisher<Integer> reducePub = reducePub(pub, 0, (a, b) -> a+b);

        mapPub.subscribe(logSub());
    }

    // 1,2,3,4,5
    // 0 -> (0,1) -> 0 + 1 = 1
    // 1 -> (1,2) -> 1 + 2 = 3
    private static Publisher<Integer> reducePub(Publisher<Integer> pub, int init, BiFunction<Integer, Integer, Integer> bf) {
        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {

                pub.subscribe(new DelegateSub(sub) {

                    int result = init;

                    @Override
                    public void onNext(Integer i) {
                        result = bf.apply(result, i);
                        System.out.println("reducePub.onNext : " + result);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }


    private static Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub) {  //adaptor pattern, 필요한 메소드만 override 해서 사용하기 위함
                    @Override
                    public void onNext(Integer i){
                        sub.onNext(f.apply(i));
                    }
                });
            }
        };
    }


    private static Subscriber<Integer> logSub() {
        return new Subscriber<Integer>() {

            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println("onSubscribe");
                this.subscription = subscription;
                this.subscription.request(100);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println("onNext : " + item);
                //this.subscription.request(1);

            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError : " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };
    }

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub) {

                    int sum = 0;

                    @Override
                    public void onNext(Integer i) {
                        System.out.println("sumPub.onNext : " + i);
                        sum += i;
                    }

                    @Override
                    public void onComplete() {

                        System.out.println("sumPub.onComplete");

                        sub.onNext(sum);
                        sub.onComplete();
                    }
                });
            }
        };
    }


    private static Publisher<Integer> interPub(List<Integer> iter) {
        return new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber subscriber) {
                Iterable<Integer> itr = iter;
                Iterator<Integer> it = itr.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        int i = 0;
                        try {
                            while (i++ < n) {
                                if(it.hasNext()) {
                                    subscriber.onNext(it.next());
                                } else {
                                    subscriber.onComplete();
                                    break;
                                }
                            }
                        } catch(RuntimeException e) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };
    }
}
