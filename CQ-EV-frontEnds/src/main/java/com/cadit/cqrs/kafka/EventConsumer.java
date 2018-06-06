package com.cadit.cqrs.kafka;

import com.cadit.data.ActionWrapper;
import com.cadit.data.DocEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

/**
 * ****************************************************FRONT-END*******************************************
 * ********************************************************************************************************
 * Ogni consumer ha il suo consumer-ID per leggere da tutte le partizioni (share delle partizioni)
 * Per esempio provare a avviare un Comnsumer anche da linea di comando (che ha un consumer_id diverso):
 *
 * kafka_2.11-1.1.0\bin\windows> kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic TEST3 --from-beginning
 * (--from-beginig torna indietro e legge dalla prima partizione, è opzionale )
 * così vedi che legge gli stessi messaggi che legge anche da questo EventConsumer (ovvero l'id della stampa)
 *
 * Altrimenti è possbile specificare le partizioni per consumer  ( in tal caso arrivano messaggi distinti come se legessi da più topics):
 * Oneroso avere più topic, potrebbe essere fattibile dividersi le partizioni?
 * Se si fa sul consumer come fare ad assegnare anche ai producer una partizione di interesse?
 * Zookeper si occupa proprio di bilanciare le partizioni tramite metodo round-robin
 *
 *   String topic = "nome della topic";
 *        TopicPartition partition0 = new TopicPartition(topic, 0);
 *        TopicPartition partition1 = new TopicPartition(topic, 1);
 *        consumer.assign(Arrays.asList(partition0, partition1));
 *
 *     Topic, partition files li trovate in C:\tmp\kafka-logs e i log di zookeper in c:\tmp\zookper
 *     a meno che dalla configurazione che avete passato ai bat o sh abbiate specificato diversi percorsi
 */
public class EventConsumer implements Runnable{

    private final ActionWrapper eventConsumer;
//    private final KafkaConsumer<String, DocEvent> consumer;
    private final KafkaConsumer<String, String> consumer;


    private AtomicBoolean closed = new AtomicBoolean();
    private Logger log = Logger.getLogger(this.getClass().getName());

    public EventConsumer(Properties kafkaProperties, ActionWrapper eventConsumer, String... topics) {
        this.eventConsumer = eventConsumer;
        consumer = new KafkaConsumer(kafkaProperties);
        consumer.subscribe(asList(topics)); //lista delle topic
    }


    @Override
    public void run() {
        try {
            while (!closed.get()) {
                consume();
            }
        } catch (WakeupException e) {
            //  wakeup Exception quando esternamente invoco close sul consumer (vedi stop sotto)
            e.printStackTrace();
        } catch (Exception e) {
            log.severe("si è verificata una Exception che non è una wakeupException! " + e.getMessage());
            e.printStackTrace();
        } finally {
            consumer.close();
            log.info("Consumer stopped! closed: " + closed.get());
        }
    }

    private void consume() {
        ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE); //not threadsafe! PULL MODE sulla topic
        for (ConsumerRecord<String, String> record : records) {
            printConsumerInfo(record);
            DocEvent docEvent = new DocEvent(Integer.valueOf(record.value()));
            eventConsumer.accept(docEvent);
        }
        consumer.commitAsync();
//        consumer.commitSync(); //solo per At-LEAST_ONCE, in questo caso non usato perchè uso l'autocommit
    }

    private void printConsumerInfo(ConsumerRecord<String, String> record) {
        log.info( String.format("*C : Consumer Record:(%s, %s, %d, %d)",
                record.key(), record.value(),
                record.partition(), record.offset()));
    }

    public void stop() {
        log.warning("STOP CONSUMER");
        closed.set(true);
        consumer.wakeup();
    }



    /**
     * funzionerà? Assegno le partizioni a mano per consumer.
     * Come imposto invece
     * dal producer la partizione a cui scrivere?
     * @param topics
     */
//    public void assignPartitionperTopicToConsumer(String... topics){
//        List<String> topicList = (List<String>) asList(topics);
//        //lista delle partizioni: http://cloudurable.com/blog/kafka-tutorial-kafka-producer-advanced-java-examples/index.html
//        for (String topic : topicList){
//            TopicPartition partitionPerEvento1 = new TopicPartition(topic, 0);
//            TopicPartition partitionPerEvento2 = new TopicPartition(topic, 1);
//            consumer.assign(asList(partitionPerEvento1, partitionPerEvento2));
//        }
//    }

}
