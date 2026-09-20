package taskplanner.summarizationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import taskplanner.summarizationservice.dto.summarizattion.request.SummarizationRequest;
import taskplanner.summarizationservice.dto.summarizattion.response.SummarizationResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaService {

    private final SummarizationService summarizationService;

    @SendTo
    @KafkaListener(topics = KafkaTopics.SUMMARIZATION_REQUESTS)
    public SummarizationResponse consume(SummarizationRequest dto) {
        log.info("received data for summarization");

        return summarizationService.getSummary(dto);
    }
}

