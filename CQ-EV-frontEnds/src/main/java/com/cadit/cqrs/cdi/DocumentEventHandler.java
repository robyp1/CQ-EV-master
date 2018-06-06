package com.cadit.cqrs.cdi;

import com.cadit.data.ActionWrapper;
import com.cadit.data.DocEvent;
import com.cadit.data.KafkaProperties;
import com.cadit.cqrs.kafka.EventConsumer;
import com.cadit.util.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * *******************************************FRONT-END********************************
 * Gestisce l'arrivo di un evento localmente
 * EJB  singleton
 * **************************************************************************************
 */
@Startup
@Singleton
public class DocumentEventHandler {

    public static final String DOC_KAFKA_TOPIC_NAME = "DOC-KAFKA-TOPIC-NAME";
    public static final String DEFAULT_DOC_KAFKA_TOPIC_NAME = "doc-event-handler";
    private static final String DEFAULT_KAFKA_TOPIC_GROUPID = "doc.group1.consumer.kafka";
    public static final String DOC_KAFKA_TOPIC_GROUPID = "DOC_KAFKA_TOPIC_GROUPID";
    private EventConsumer eventConsumer;

    @Resource
    ManagedExecutorService mes; //non JEE6, JEE7 e sucessiva

    @Inject @KafkaProperties
    Properties kafkaProperties;

    @Inject
    Event<DocEvent> events;

    @Inject
    Configuration conf;


    private Logger log = Logger.getLogger(this.getClass().getSimpleName());
    private String topic_name;


    /**
     * Handler su Event .fire
     * @param event
     */
    public void handle(@Observes DocEvent event) {
        log.info("E'' arrivato l'evento dalla topic " + event);
    }


    @PostConstruct
    private void init() {

        //istanzia il consumer
        Properties consumerkafkaProperties = kafkaProperties;
        String topic_name = conf.getConf().getProperty(DOC_KAFKA_TOPIC_NAME, DEFAULT_DOC_KAFKA_TOPIC_NAME);
        kafkaProperties.put("topics.doc", topic_name);

        String groupID = conf.getConf().getProperty(DOC_KAFKA_TOPIC_GROUPID, DEFAULT_KAFKA_TOPIC_GROUPID);
        consumerkafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);

        //polling event dalla topic
        eventConsumer = new EventConsumer(consumerkafkaProperties, new ActionWrapper(events), topic_name);

        mes.execute(eventConsumer);
    }




    /**
     * Quando si ferma l'app server o si fa l'undeploy il bean viene eliminato
     * però il kafka consumer è attivo in un loop e quindi va fermato, o tramite thread interrupt
     * oppure tramite WakeUpException (vedere metodo stop)
     *
     */
    @PreDestroy
    public void close() {
        log.warning("PRE-DESTROY, TRY TO STOP CONSUMER..");
        eventConsumer.stop();
    }

}
