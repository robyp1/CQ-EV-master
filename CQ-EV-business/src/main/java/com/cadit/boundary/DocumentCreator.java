package com.cadit.boundary;

import com.cadit.data.DocumentEntity;
import com.cadit.data.EventEntity;
import com.cadit.data.DocEnum;
import com.cadit.data.StatusJob;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * *************************************************************************************************
 * ********************************************BACK-END*********************************************
 * *************************************************************************************************
 */
@Stateless
public class DocumentCreator {

    @PersistenceContext
    EntityManager em;

    /**
     * metodo simulato, dovrebbe chiamare una libreria per la crezione di documenti (Pdf, Excel..)
     * Salva i riferimenti al documento creato in una transazione a parte (.REQUIRES_NEW)
     * Alla uscita del metodo committa i riferimenti al pdf e dove è stato salvato ma l'evento che commita
     * per ora è RUNNING perchè non lo ha ancora inviato alla topic
     * @param docType
     * @return
     * @throws InterruptedException
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Integer create(DocEnum docType) throws InterruptedException {
        String infoPath ="";
        EventEntity initialEventState = new EventEntity();
        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setDocType(docType);
        switch (docType){
            case PDF :  initialEventState.setStatus(StatusJob.RUNNING);
                initialEventState.setType(docType);
                Thread.sleep(5000);
                infoPath = "path/to/pdf/" + "1";
                break;
            case EXCEL:
                initialEventState.setStatus(StatusJob.RUNNING);
                initialEventState.setType(docType);
                Thread.sleep(58000);
                infoPath = "path/to/excel/" + "1";
                break;
        }
        initialEventState.setInfoPath(infoPath);
        documentEntity.addEvent(initialEventState);
        documentEntity.setPath(infoPath);
        em.persist(documentEntity);
        em.flush(); //verifico su db che non ci siano errori di constraints o di data truncation ecc...
        return  documentEntity.getId();

    }
}
