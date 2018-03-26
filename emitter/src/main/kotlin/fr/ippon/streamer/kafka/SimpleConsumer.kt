package fr.ippon.streamer.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

@Service
object SimpleConsumer {

    val basicConsumerStream: Flux<String> by lazy {
        Flux.create { sink: FluxSink<String> ->
            val configuration = kafkaConfiguration()
            val consumer: KafkaConsumer<String, String> = KafkaConsumer(configuration)
            val topic = "twitter_json"
            consumer.subscribe(setOf(topic))
            while (true) {
                val records: ConsumerRecords<String, String> = consumer.poll(100)
                records.forEach { record ->
                    val message = record.value()
                    println(message)
                    sink.next(message)
                }
            }
        }.share()
    }

    private fun kafkaConfiguration(): Map<String, Any> {
        return mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.GROUP_ID_CONFIG to "basic"
        )
    }

}