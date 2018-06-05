
# PER CONFIGURARE L'APPLICAZIONE

## PREPARARE L'AMBIENTE CON KAFKA
### scaricare kafka eseguibile .tgz versione 1.1.0

### esempio estraggo tar e salvo in :
### salvo in C:/ProgrammiStandalone/kafka-1.1.0/kafka_2.11-1.1.0/ E trovo gli eseguibili in /bin/windows/
``` cd C:/ProgrammiStandalone/kafka-1.1.0/kafka_2.11-1.1.0/bin/windows/ ```

### eseguo dal linea di comando:
### 1) avvio zookeeper
``` zookeeper-server-start.bat C:\ProgrammiStandalone\kafka-1.1.0\kafka_2.11-1.1.0\config\zookeeper.properties ```

### 2) avvio kafka broker
``` kafka-server-start.bat C:\ProgrammiStandalone\kafka-1.1.0\kafka_2.11-1.1.0\config\server.properties ```

### 3) creo la mia topic
``` kafka-topics.bat --zookeeper localhost:2128 --replication-factor 1 --partitions 1 --topic doc-event-handler ```

### 4) compilare il progetto con maven
``` mvn clean install ```

### 5) avviare il server wildfly 10 da linea di comando, andare in /bin ed eseguire
``` standalone.bat ```

### 6) fare il deploy su wildfly (attenzione la porta 9090 deve essere libera altrimenti va fatto il deploy a mano o da pagina di management)
``` wildfly:deploy ```

### i logs e eventuali componenti di kafka di default vengono salvate in C:\tmp\kafka-logs


* fonte: https://kafka.apache.org/quickstart

## PROVE MANUALI DA CMD DOS


per provare manualmente una volta tirato su il broker e zookeeper:

``` cd C:/ProgrammiStandalone/kafka-1.1.0/kafka_2.11-1.1.0/bin/windows/ ```

### 1) la topic TEST va prima creata con
``` kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic TEST ```

### 2) aprire console per inviare messaggi sulla topic TEST
``` kafka-console-producer.bat --broker-list localhost:9092 --topic TEST ```

### 3) legge dalla topic test
``` kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic TEST --from-beginning ```

## Link utili
* https://kafka.apache.org/quickstart
* http://cloudurable.com/blog/kafka-architecture-consumers/index.html
* https://dzone.com/articles/kafka-producer-in-java
* https://dzone.com/articles/writing-a-kafka-consumer-in-java?fromrel=true
* https://www.ibm.com/support/knowledgecenter/en/SSZJPZ_11.7.0/com.ibm.swg.im.iis.conn.kafka.usage.doc/topics/Setting_kafka_connector.html
* https://kafka.apache.org/documentation/#connect
* https://www.confluent.io/blog/transactions-apache-kafka/
* http://notes.stephenholiday.com/Kafka.pdf
* https://dzone.com/articles/kafka-clients-at-most-once-at-least-once-exactly-o
* http://cloudurable.com/blog/kafka-tutorial-kafka-producer-advanced-java-examples/index.html
* https://stackoverflow.com/questions/46492707/consumer-how-to-specify-partition-to-read-kafka
* https://kafka.apache.org/0110/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html
* http://zookeeper.apache.org/doc/current/zookeeperProgrammers.html#ch_programStructureWithExample

## altro
* https://dzone.com/articles/java-ee6-events-lightweight
* https://docs.oracle.com/javaee/7/tutorial/cdi-adv005.htm
* http://entjavastuff.blogspot.com/2011/02/ejb-transaction-management-going-deeper.html
* https://curator.apache.org/curator-test/

## examples:
* https://github.com/sdaschner/scalable-coffee-shop


## PREPARARE WILDFLY
Copiare il modulo org.hsql in modules/system/layers/base/org/hsql/main.

Il modulo è costituito da due file :
### module.xml
### hsqldb.jar

### Contenuto module.xml (libreria hsqldb stessa versione del pom) e dipendenze:
```
<module xmlns="urn:jboss:module:1.3" name="org.hsqldb">

    <resources>
        <resource-root path="hsqldb.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
        <module name="javax.servlet.api" optional="true"/>
    </dependencies>
</module>
```
Andare in standalone/configuration/standalone.xml ed aggiungere il datasource e i riferimenti al driver:
```
 <datasource jta="true" jndi-name="java:jboss/jdbc/testDS" pool-name="testDS" enabled="true" use-java-context="true">
        <connection-url>jdbc:hsqldb:hsql://localhost:9001/testDS</connection-url>
        <driver>hsqldb</driver>
        <security>
            <user-name>sa</user-name>
        </security>
    </datasource>
    <drivers>
        ...
        <driver name="hsqldb" module="org.hsqldb">
            <driver-class>org.hsqldb.jdbc.JDBCDriver</driver-class>
        </driver>
    </drivers>
```

**- Scaricare il server hsql (stessa versione della libreria jar), eventualmente come client scaricare Squirell



### Esempio di invio e ricezione del messaggio

```
10:39:05,333 WARN  [org.apache.kafka.clients.producer.ProducerConfig] (EJB default - 2) The configuration 'key.deserializer' was supplied but isn't a known config.
10:39:05,333 WARN  [org.apache.kafka.clients.producer.ProducerConfig] (EJB default - 2) The configuration 'value.deserializer' was supplied but isn't a known config.
10:39:05,333 WARN  [org.apache.kafka.clients.producer.ProducerConfig] (EJB default - 2) The configuration 'topics.doc' was supplied but isn't a known config.
10:39:05,333 INFO  [org.apache.kafka.common.utils.AppInfoParser] (EJB default - 2) Kafka version : 1.1.0
10:39:05,334 INFO  [org.apache.kafka.common.utils.AppInfoParser] (EJB default - 2) Kafka commitId : fdcf75ea326b8e07
10:39:05,342 INFO  [org.apache.kafka.clients.Metadata] (kafka-producer-network-thread | KafkaDocProducer) Cluster ID: eKbilkMOSFGyLGUnoSnMwA
10:39:05,369 INFO  [com.cadit.kafka.EventProducer] (EJB default - 2) *P : sent record(key=KafkaDocProducer-msg-1 value=1) meta(partition=0, offset=7) time=33

10:39:05,370 INFO  [com.cadit.cqrs.kafka.EventConsumer] (EE-ManagedExecutorService-default-Thread-1) *C : Consumer Record:(KafkaDocProducer-msg-1, 1, 0, 7)
10:39:05,370 INFO  [DocumentEventHandler] (EE-ManagedExecutorService-default-Thread-1) E'' arrivato l'evento dalla topic DocEvent{eventTime=java.util.GregorianCalendar[time=1528187945370,areFieldsSet=true,areAllFieldsSet=true,lenient=true,zone=sun.util.calendar.ZoneInfo[id="Europe/Berlin",offset=3600000,dstSavings=3600000,useDaylight=true,transitions=143,lastRule=java.util.SimpleTimeZone[id=Europe/Berlin,offset=3600000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=2,startMonth=2,startDay=-1,startDayOfWeek=1,startTime=3600000,startTimeMode=2,endMode=2,endMonth=9,endDay=-1,endDayOfWeek=1,endTime=3600000,endTimeMode=2]],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=1,YEAR=2018,MONTH=5,WEEK_OF_YEAR=23,WEEK_OF_MONTH=2,DAY_OF_MONTH=5,DAY_OF_YEAR=156,DAY_OF_WEEK=3,DAY_OF_WEEK_IN_MONTH=1,AM_PM=0,HOUR=10,HOUR_OF_DAY=10,MINUTE=39,SECOND=5,MILLISECOND=370,ZONE_OFFSET=3600000,DST_OFFSET=3600000], documentID=1}
```