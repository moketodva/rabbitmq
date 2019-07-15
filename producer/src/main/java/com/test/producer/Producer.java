package com.test.producer;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author moke
 */
@Component
public class Producer {

    @Resource
    private RabbitTemplate rabbitTemplate;
    private AtomicInteger toExchangeFailCount = new AtomicInteger(0);
    private AtomicInteger toExchangeAckCount = new AtomicInteger(0);
    private AtomicInteger toQueueFailCount = new AtomicInteger(0);

    @PostConstruct
    private void rabbitTemplateInit(){
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            System.out.println(correlationData);
            if(ack){
                toExchangeAckCount.incrementAndGet();
            }else{
                toExchangeFailCount.incrementAndGet();
                // TODO 失败的进行记录
            }
        });
        rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) ->
            toQueueFailCount.incrementAndGet()
        );
    }

    public void produce() {
        // 发送消息
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String phone = "13132223123";
        Message message = MessageBuilder.withBody(phone.getBytes()).setContentType(MessageProperties.CONTENT_TYPE_JSON).setMessageId(uuid).build();
        CorrelationData correlationData = new CorrelationData(uuid);
        correlationData.setReturnedMessage(message);
        System.out.println(uuid);
        rabbitTemplate.convertAndSend("exchange", "sms", message, correlationData);
    }

    public AtomicInteger getToExchangeFailCount() {
        return toExchangeFailCount;
    }

    public AtomicInteger getToQueueFailCount() {
        return toQueueFailCount;
    }

    public AtomicInteger getToExchangeAckCount() {
        return toExchangeAckCount;
    }
}
