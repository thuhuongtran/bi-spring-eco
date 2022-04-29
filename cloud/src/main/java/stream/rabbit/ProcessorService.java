package stream.rabbit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class ProcessorService {
    @Autowired
    private MyProcessor processor;

    @StreamListener(MyProcessor.INPUT)
    public void routeValues(Integer val) {
        if (val < 10) {
            processor.anOutput().send(message(val));
        } else {
            processor.anotherOutput().send(message(val));
        }
    }

    @StreamListener(
            target = MyProcessor.INPUT,
            condition = "payload < 10")
    public void routeValuesToAnOutput(Integer val) {
        processor.anOutput().send(message(val));
    }

    @StreamListener(
            target = MyProcessor.INPUT,
            condition = "payload >= 10")
    public void routeValuesToAnotherOutput(Integer val) {
        processor.anotherOutput().send(message(val));
    }

    private static final <T> Message<T> message(T val) {
        return MessageBuilder.withPayload(val).build();
    }
}
