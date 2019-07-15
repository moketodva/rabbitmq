package com.test.producer;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author moke
 */
public class Task implements Runnable {

    private CountDownLatch countDownLatch;
    private CyclicBarrier cyclicBarrier;
    private Producer producer;

    public Task(CountDownLatch countDownLatch, CyclicBarrier cyclicBarrier, Producer producer) {
        this.countDownLatch = countDownLatch;
        this.cyclicBarrier = cyclicBarrier;
        this.producer = producer;
    }

    @Override
    public void run() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i = 0;
        while(i < 1){
            producer.produce();
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
//        try {
//            producer.produce();
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
