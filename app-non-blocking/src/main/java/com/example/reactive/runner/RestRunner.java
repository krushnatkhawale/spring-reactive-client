package com.example.reactive.runner;

import com.example.reactive.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class RestRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestRunner.class);

    @Value("${endpoint}")
    private String getAllEndpoint;

    @Autowired
    WebClient webClient;

    @Override
    public void run(String... args) throws Exception {
        Flux<Resource> resourceFlux = webClient.get()
                .uri(getAllEndpoint)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(this::getResourceFlux);
        processFlux(resourceFlux);
    }

    private Flux<Resource> getResourceFlux(ClientResponse cr) {
        return cr.bodyToFlux(Resource.class);
    }

    private void processFlux(Flux<Resource> flux) {
        flux.subscribe(this::logResponses, this::errorHandler, this::responseCompleter);
    }

    private void logResponses(Resource resource) {
        String resourceAsString = resource.toString();
        LOGGER.info(resourceAsString);
    }

    private void errorHandler(Throwable throwable) {
        LOGGER.error("Error: {}", throwable.getMessage());
    }

    private Runnable responseCompleter() {
        return () -> LOGGER.info("DONE");
    }
}