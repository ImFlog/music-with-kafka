package fr.ippon.kafka.streams.domains;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwitterUserMessage {

    private String name;
    private String message;

}
