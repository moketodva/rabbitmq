package com.test.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author moke
 */
@Component
public class Consumer {

    private AtomicInteger dlxCount = new AtomicInteger(0);

    @RabbitHandler
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms_queue", durable = "true",
                    arguments = {
                            @Argument(name = "x-max-priority", value = "9", type = "java.lang.Long"),
                            @Argument(name = "x-overflow", value = "reject-publish"),
                            @Argument(name = "x-max-length", value = "999", type = "java.lang.Long"),
                            @Argument(name = "x-dead-letter-exchange", value = "dlx_exchange"),
                            @Argument(name = "x-dead-letter-routing-key", value = "sms")
                    }),
            exchange = @Exchange(name = "exchange"),
            key = "sms")
    )
    public void consume(@Payload String phone, @Headers Map<String, Object> properties, Channel channel) throws IOException {
        Long deliveryTag = (Long) properties.get(AmqpHeaders.DELIVERY_TAG);
        try {
            // 如有幂等性要求，可以往Message里加入messageId，借助messageId解决幂等性问题
            // 调用短信第三方接口
            System.out.println(phone);
            System.out.println(properties);
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitHandler
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "dlx_sms_queue", durable = "true",
                    arguments = {
                            @Argument(name = "x-max-priority", value = "9", type = "java.lang.Long"),
                            @Argument(name = "x-max-length", value = "9999", type = "java.lang.Long"),
                    }),
            exchange = @Exchange(name = "dlx_exchange"),
            key = "sms")
    )
    public void dlxConsume(@Payload String phone, @Headers Map<String, Object> properties, Channel channel) throws IOException {
        Long deliveryTag = (Long) properties.get(AmqpHeaders.DELIVERY_TAG);
        int i = dlxCount.incrementAndGet();
        try {
            // 调用短信第三方接口
            System.out.println(phone);
            System.out.println(properties);
            System.out.println("处理第" + i + "条死信");
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
