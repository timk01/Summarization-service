package taskplanner.summarizationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import taskplanner.summarizationservice.service.KafkaTopics;

@Configuration
public class TopicConfiguration {

    @Bean
    public NewTopic summarySendingTopic() {
        return TopicBuilder
                .name(KafkaTopics.SUMMARIZATION_REQUESTS)
                .partitions(1)
                .replicas(1)
                .build();
    }
}