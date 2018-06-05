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
     * Se il documento è stato creato salva l'evento e invia l'evento al front end
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRED) //indifferentem se non cè una tx in corso ne crea una nuova
    public void pdfCreator(){
        EventEntity finalEventState = new EventEntity();
        try {
            DocEnum type = DocEnum.PDF;
            Integer docId = documentCreator.create(type);//oggetto detached perchè appartiene ad una altra transazione
            DocumentEntity documentEntity = em.find(DocumentEntity.class, docId);
            finalEventState.setType(type);
            finalEventState.setStatus(StatusJob.COMPLETED); //qui imposto che il PDF è stato creato ma commito solo dopo avere inviatto l'evento in coda (all'uscita del metodo jta committa)
            finalEventState.setInfoPath(documentEntity.getPath());
            documentEntity.addEvent(finalEventState);
            em.persist(finalEventState);

            DocEvent docEvent = new DocEvent(documentEntity.getId());
            //invio evento come msg alla topic, dopo posso committare
            eventProducer.publish(docEvent);

            //throw new RuntimeException("Ops! runtime ex forzata per provare che ha terminato la transazione interna e ha fatto rollback solo di questa");
            //NB: la inner transaction ha già salvato i riferimenti al pdf/doc che è stato completato, ci perdiamo solo l'evento di comunicazione al front-end
            //Il client ha a dispozione il documento comunque tramite una pagina di query /interrogazioen

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
