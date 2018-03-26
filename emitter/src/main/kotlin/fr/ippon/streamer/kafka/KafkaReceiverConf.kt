package fr.ippon.streamer.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.LongDeserializer
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
    private val SOUNDS = "sounds"
    private val CHARTS = "charts"
    private val USERS = "users"

    @Bean(name = ["soundsReceiver"])
    fun soundsReceiver(): KafkaReceiver<String, String> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<String, String>(props)
                .subscription(setOf(SOUNDS))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToBeginning) }
        return KafkaReceiver.create(options)
    }

    @Bean(name = ["chartsReceiver"])
    fun chartsReceiver(): KafkaReceiver<String, String> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<String, String>(props)
                .subscription(setOf(CHARTS))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToBeginning) }
        return KafkaReceiver.create(options)
    }

    @Bean(name = ["usersReceiver"])
    fun usersReceiver(): KafkaReceiver<String, String> {
        val props: Map<String, Any> = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
                ConsumerConfig.GROUP_ID_CONFIG to groupID,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        val options = ReceiverOptions
                .create<String, String>(props)
                .subscription(setOf(USERS))
                .addAssignListener { p -> p.forEach(ReceiverPartition::seekToBeginning) }
        return KafkaReceiver.create(options)
    }


}
