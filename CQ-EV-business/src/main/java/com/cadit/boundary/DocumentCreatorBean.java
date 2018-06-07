package com.cadit.boundary;

import com.cadit.data.*;
import com.cadit.kafka.EventProducer;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * *************************************************************************************************
 * ********************************************BACK-END*********************************************
 * *************************************************************************************************
 * EJB Session di tipo Stateless
 * si occupata di lanciare in maniera asincrona la 'libreria' che cre il documento (pdf o excel) e di far pubblicare
 * l'evento quando lo stesso è pronto.
 */
@Stateless
@Remote(DocumentRemote.class)
@TransactionManagement(value=TransactionManagementType.CONTAINER) //esplicito ma non necessario
public class DocumentCreatorBean implements DocumentRemote{

    @EJB //questo ejb è locale
    DocumentCreator documentCreator;

    @Inject
    EventProducer eventProducer;

    @Resource
    SessionContext ejbSessionContext;

    @PersistenceContext
    EntityManager em;

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    /**
     * metodo esposto da remoto con http-remoting porta 8080 del server wildfly 1 dove è deploiato questo modulo di business
     * Se il documento è stato creato salva l'evento e invia l'evento al front end
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) //se chiamo remotamente sembra sia obbligatorio esplicitare new altrimenti mi da errore che non cè una transazione associata
    public void pdfCreator(){
        EventEntity finalEventState = new EventEntity();
        try {
            DocEnum type = DocEnum.PDF;
            Integer docId = documentCreator.create(type);//oggetto detached perchè ritorna da un metoto che apre una altra transazione
            DocumentEntity documentEntity = em.find(DocumentEntity.class, docId);//oggetto re-attached
            finalEventState.setType(type);
            finalEventState.setStatus(StatusJob.COMPLETED); //qui imposto che il PDF è stato creato ma commito solo dopo avere inviato l'evento in coda (all'uscita del metodo jta fa commit)
            finalEventState.setInfoPath(documentEntity.getPath());
            documentEntity.addEvent(finalEventState);
            em.persist(finalEventState);
            DocEvent docEvent = new DocEvent(documentEntity.getId()); //transazione interna era required new quindi ha completato e ho gia' l'id generato da db
            //invio evento come msg alla topic, dopo posso committare
            eventProducer.publish(docEvent);
            //throw new RuntimeException("Ops! runtime ex forzata per provare che ha terminato la transazione interna e ha fatto rollback solo di questa");
            //NB: la inner transaction ha già salvato i riferimenti al pdf/doc che è stato completato, ci perdiamo solo l'evento di comunicazione al front-end
            //Il client ha a dispozione il documento comunque tramite una pagina di query /interrogazione
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            ejbSessionContext.setRollbackOnly(); //se il pdf non si è cereato forzo rollback senza rilanciare Runtime che cmq lo farebbe
        }
    }


    /**
     * metodo di utilità
     * @param docId
     */
//    public void removeAllEventsByDocument(Integer docId){
//        DocumentEntity documentEntity = em.find(DocumentEntity.class, docId);
//        for (EventEntity ev : documentEntity.getEventIds()) {
//            em.remove(ev); //non usando cascade delete devo scegliere uno per uno quali cancellare, qui tutte quindi sotto faccio clear
//        }
//        documentEntity.getEventIds().clear();//rimuovo dalla lista tutto
//        em.merge(documentEntity);  //faccio il merge di ciò che non è in cascade, se ho fatto remove nella lista ottengo cmq la rimozione
//    }

}
