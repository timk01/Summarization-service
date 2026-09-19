package taskplanner.summarizationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import taskplanner.summarizationservice.dto.SummarizationRequest;
import taskplanner.summarizationservice.response.SummaryResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaService {

    private final SummarizationService summarizationService;

    @SendTo
    @KafkaListener(topics = KafkaTopics.SUMMARIZATION_REQUESTS)
    public SummaryResponse consume(SummarizationRequest dto) {
        log.info("received data for summarization");

        return summarizationService.getSummary(dto);
    }
}

