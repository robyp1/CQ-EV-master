package com.cadit.util;

import com.cadit.data.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Properties;

/**
 * Valorizza e restituisce le properties di base (comuni a consumer e Producer)
 * Tipo di configurazione utilizzata AT-MOST-ONCE (autocommit abilitato e autocommit.interval.ms basso)
 * @return
 */
@ApplicationScoped
public class KafkaSettings {


    @Inject
    Configuration conf;


    @Produces
    @KafkaProperties
    public Properties getKafkaProperties() {


        Properties kafkaProperties = new Properties();
        /**** imposto le proprietà comuni a polling consumer e producer kafka***/
        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DocEventDeserializer.class.getName());
        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DocEventSerializer.class.getName());
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //        kafkaProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); di default è a true come
//        kafkaProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000"); di default è 5000, vedi doc akfka
        //lista dei server broker del cluster o del server ip:porta dove gira il server kafka (broker)
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return kafkaProperties;
    }

}
