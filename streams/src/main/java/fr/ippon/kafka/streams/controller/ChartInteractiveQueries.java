package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.topologies.ChartsTopology;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@RestController
public class ChartInteractiveQueries {

    private ChartsTopology chartsTopology;

    public ChartInteractiveQueries(ChartsTopology chartsTopology) {
        this.chartsTopology = chartsTopology;
    }

    @GetMapping("/charts")
    public Mono<Map<String, Long>> getCountByCategories() {
        return Flux
                .fromStream(chartsTopology.getChartsCountPerCategories())
                .collect(toMap(kv -> kv.key, kv -> kv.value));
    }

}
