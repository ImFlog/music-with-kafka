package fr.ippon.streamer.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.receiver.ReceiverPartition

@Configuration
class KafkaReceiverConf {

    private val bootstrapServer = "localhost:9092"
    private val groupID = "sample-group"
    private val topic = "sounds"

    @Bean
    fun kafkaDataReceiver(): KafkaReceiver<Int, String> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to IntegerDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<Int, String>(props)
                .subscription(setOf(topic))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToBeginning) }
        return KafkaReceiver.create(options)
    }

}
