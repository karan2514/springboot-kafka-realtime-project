package net.javaguides.springboot.springboot;

import com.launchdarkly.eventsource.ConnectStrategy;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;
import com.launchdarkly.eventsource.background.BackgroundEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Service
public class WikimediaChangesProducer {
    @Value("${spring.kafka.topic.name}")
private String topicName;
    private static final Logger LOGGER = LoggerFactory.getLogger(WikimediaChangesProducer.class);

    private KafkaTemplate<String, String> kafkaTemplate;

    public WikimediaChangesProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage() throws InterruptedException {


        // to read real time stream data from wikimedia, we use event source
        BackgroundEventHandler backgroundEventHandler = new WikimediaChangesHandler(kafkaTemplate, topicName);
        {
            String url = "https://stream.wikimedia.org/v2/stream/recentchange";
            //EventSource.Builder builder = new EventSource.Builder(backgroundEventHandler,   );
            BackgroundEventSource backgroundEventSource = new BackgroundEventSource.Builder(backgroundEventHandler,
                    new EventSource.Builder(ConnectStrategy
                            .http(URI.create(url)
                            )
                    )
            ).build();
            backgroundEventSource.start();
            //TimeUnit.MINUTES.sleep(10);

            try {
                TimeUnit.MINUTES.sleep(10);
            } catch (InterruptedException e) {
                LOGGER.error(String.format("Error: %e", e.getMessage()));
                throw new RuntimeException("An error has ocurred");
            }
        }

   /* public void sendMessage() throws InterruptedException {
        String topic = "wikimedia_recentchange";
        // to read real time stream data from wikimedia, we use event source
        EventHandler eventHandler = new WikimediaChangesHandler(kafkaTemplate, topic);
        String url = "https://stream.wikimedia.org/v2/stream/recentchange";
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url));
        EventSource eventSource = builder.build();
        eventSource.start();

        TimeUnit.MINUTES.sleep(10);
    }*/
    }
}


