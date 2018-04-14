package fr.ippon.kafka.streams.controller;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChartInteractiveQueries {
//
//    private ChartsTopology chartsTopology;
//
//    public ChartInteractiveQueries(ChartsTopology chartsTopology) {
//        this.chartsTopology = chartsTopology;
//    }

//    @GetMapping("/charts/{category}")
//    public Mono<Map<String, Long>> getCountByCategories(@PathVariable("category") String category) {
//        return Flux
//                .fromStream(chartsTopology.getChartsCountPerCategories(category))
//                .collect(toMap(kv -> Commons.milliToDateString(kv.key), kv -> kv.value));
//    }
//
//    @GetMapping("/charts")
//    public Flux<Chart> getLastChart() {
//        return Flux.fromStream(chartsTopology.getLastChartSong());
//    }

}
