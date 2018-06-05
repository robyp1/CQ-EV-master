package com.cadit.kafka;

import com.cadit.data.DocEvent;
import com.cadit.data.KafkaProperties;
import com.cadit.util.Configuration;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * *************************************************************************************************
 * ********************************************BACK-END*********************************************
 * *************************************************************************************************
 */
@ApplicationScoped
public class EventProducer {

//    private Producer<String, DocEvent> producer;
    private Producer<String, String> producer;
    private String topic;
    public static final String DOC_KAFKA_TOPIC_NAME = "DOC-KAFKA-TOPIC-NAME";
    public static final String DEFAULT_DOC_KAFKA_TOPIC_NAME = "doc-event-handler";

    @Inject @KafkaProperties
    Properties kafkaProperties;

    @Inject
    Configuration conf;

    private Logger log = Logger.getLogger(this.getClass().getName());
    private AtomicInteger count = new AtomicInteger(0);

    @PostConstruct
    private void init() {
        Properties ProduceProperties = kafkaProperties;
        ProduceProperties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaDocProducer");
        String topic_name = conf.getConf().getProperty(DOC_KAFKA_TOPIC_NAME, DEFAULT_DOC_KAFKA_TOPIC_NAME);
        kafkaProperties.put("topics.doc", topic_name);
//        ProduceProperties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "T1");  //solo pre configurazione at least-once, da verificare
//        ProduceProperties.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "40000");
//        ProduceProperties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG , "true");
        producer = new KafkaProducer(ProduceProperties);
        topic = kafkaProperties.getProperty("topics.doc");
//        producer.initTransactions();//solo pre configurazione
    }

    public void publish(DocEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord(topic, String.valueOf("KafkaDocProducer-msg-" + (count.incrementAndGet())) , String.valueOf(event.getDocumentID()));
        long time = System.currentTimeMillis();
        try {
//            producer.beginTransaction();  //solo pre configurazione
            Future<RecordMetadata> result = producer.send(record); //asincrono and thread-safe
            try {
                RecordMetadata recordMetadata = result.get(); //questo è bloccante perchè get sul Future
                printProducerDebugInfo(time, recordMetadata, record);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
//            producer.commitTransaction();//solo pre configurazione, decido io di committare se non committo il messaggio viene rinviato dal producer
        } catch (ProducerFencedException e) {
            producer.flush();
            producer.close();
        } catch (KafkaException e) {
//            producer.abortTransaction();//solo pre configurazione
            e.printStackTrace();
        }
    }

    private void printProducerDebugInfo(long time, RecordMetadata metadata, ProducerRecord record) {
        log.info(String.format("*P : sent record(key=%s value=%s) " +
                        "meta(partition=%d, offset=%d) time=%d\n",
                record.key(), record.value(), metadata.partition(),
                metadata.offset(), (System.currentTimeMillis() - time)));
    }

    @PreDestroy
    public void close() {
        producer.close();
    }

}
