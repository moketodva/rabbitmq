package com.test.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProducerApplicationTests {

    @Resource
    private Producer producer;
    private static final int CONCURRENCY_NUM = 1;
    private CountDownLatch countDownLatch = new CountDownLatch(CONCURRENCY_NUM);
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(CONCURRENCY_NUM + 1);

    @Test
    public void contextLoads() throws BrokenBarrierException, InterruptedException {
        Task task = new Task(countDownLatch, cyclicBarrier, producer);
        for (int i = 0; i < CONCURRENCY_NUM; i++){
            new Thread(task).start();
            countDownLatch.countDown();
        }
        cyclicBarrier.await();
        Thread.sleep(3000);
        System.out.println("ToExchangeFailCount: " + producer.getToExchangeFailCount());
        System.out.println("ToExchangeAckCount: " + producer.getToExchangeAckCount());
        System.out.println("ToQueueFailCount: " + producer.getToQueueFailCount());
    }

}
