package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.domains.twitter.TwitterUserInfo;
import fr.ippon.kafka.streams.topologies.UsersTopology;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RestController
public class UserInteractiveQueries {

    private UsersTopology usersTopology;

    public UserInteractiveQueries(UsersTopology usersTopology) {
        this.usersTopology = usersTopology;
    }

    @GetMapping(value = "/users")
    public Flux<TwitterUserInfo> users() {
        return Flux.fromStream(usersTopology.getTwitterUserInfoStream());
    }

    @RequestMapping(value = "/tweets")
    public Mono<Map<String, Long>> getTweetCountPerUser(@RequestParam(required = false) Integer count) {
        if (count == null) {
            count = Integer.MAX_VALUE;
        }
        return usersTopology
                .getTweetCountStream()
                .limit(count)
                .sorted(Comparator.comparingLong(kv -> kv.value))
                .collect(collectingAndThen(toMap(kv -> kv.key, kv -> kv.value), Mono::just));

    }

}
