package fr.ippon.streamer.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.receiver.ReceiverPartition
import reactor.kafka.receiver.ReceiverRecord

@Configuration
class KafkaReceiverConf {

    private val bootstrapServer = "localhost:9092"
    private val groupID = "sample-group"
    private val SOUNDS = "sounds"
    private val CHARTS = "charts"
    private val USERS = "users"
    private val USER_MESSAGE = "user-message"

    @Bean(name = ["soundsReceiver"])
    fun soundsReceiver(): Flux<ReceiverRecord<String, String>> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<String, String>(props)
                .subscription(setOf(SOUNDS))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToEnd) }
        return KafkaReceiver.create(options).receive()
    }

    @Bean(name = ["chartsReceiver"])
    fun chartsReceiver(): Flux<ReceiverRecord<String, String>> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<String, String>(props)
                .subscription(setOf(CHARTS))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToEnd) }
        return KafkaReceiver.create(options).receive()
    }

    @Bean(name = ["usersReceiver"])
    fun usersReceiver(): Flux<ReceiverRecord<String, String>> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<String, String>(props)
                .subscription(setOf(USERS))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToEnd) }
        return KafkaReceiver.create(options).receive()
    }

    @Bean(name = ["userMessageReceiver"])
    fun userMessageReceiver(): Flux<ReceiverRecord<String, String>> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<String, String>(props)
                .subscription(setOf(USER_MESSAGE))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToEnd) }
        return KafkaReceiver.create(options).receive()
    }

}
