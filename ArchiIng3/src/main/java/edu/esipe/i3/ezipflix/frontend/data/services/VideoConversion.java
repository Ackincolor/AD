package edu.esipe.i3.ezipflix.frontend.data.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.esipe.i3.ezipflix.frontend.ConversionRequest;
import edu.esipe.i3.ezipflix.frontend.ConversionResponse;
import edu.esipe.i3.ezipflix.frontend.data.entities.VideoConversions;
import edu.esipe.i3.ezipflix.frontend.data.repositories.VideoConversionRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;

import java.util.UUID;

/**
 * Created by Gilles GIRAUD gil on 11/4/17.
 */
@Service
public class VideoConversion {

//    @Value("${conversion.messaging.rabbitmq.conversion-queue}") public  String conversionQueue;
//    @Value("${conversion.messaging.rabbitmq.conversion-exchange}") public  String conversionExchange;


    //@Autowired RabbitTemplate rabbitTemplate;
    @Autowired PubSubTemplate pubSubTemplate;

    @Autowired VideoConversionRepository videoConversionRepository;

//    @Autowired
//    @Qualifier("video-conversion-template")
//    public void setRabbitTemplate(final RabbitTemplate template) {
//        this.rabbitTemplate = template;
//    }

    public void save(
                final ConversionRequest request,
                final ConversionResponse response) throws JsonProcessingException {

        final VideoConversions conversion = new VideoConversions(
                                                    response.getUuid().toString(),
                                                    request.getPath().toString(),
                                                    "");

        videoConversionRepository.save(conversion);
        final Message message = new Message(conversion.toJson().getBytes(), new MessageProperties());
        //rabbitTemplate.convertAndSend(conversionExchange, conversionQueue,  conversion.toJson());
        this.pubSubTemplate.publish("my-topic", conversion.toJson());
    }

}
