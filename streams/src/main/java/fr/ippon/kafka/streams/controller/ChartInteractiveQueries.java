package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.domains.Chart;
import fr.ippon.kafka.streams.topologies.ChartsTopology;
import fr.ippon.kafka.streams.utils.Commons;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/charts/{category}")
    public Mono<Map<String, Long>> getCountByCategories(@PathVariable("category") String category) {
        return Flux
                .fromStream(chartsTopology.getChartsCountPerCategories(category))
                .collect(toMap(kv -> Commons.milliToDateString(kv.key), kv -> kv.value));
    }

    @GetMapping("/charts")
    public Flux<Chart> getLastChart() {
        return Flux.fromStream(chartsTopology.getLastChartSong());
    }

}
