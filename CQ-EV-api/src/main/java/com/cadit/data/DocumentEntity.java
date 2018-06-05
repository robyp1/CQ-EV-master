package com.cadit.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * *************************************************************************************************
 * ********************************************API*********************************************
 * *************************************************************************************************
 */
@Entity
@Table(name = "DOC_STORE")
 public class DocumentEntity {

    private List<EventEntity> eventIds = new ArrayList<EventEntity>();//più righe in EventEntity

    private String path;

    private DocEnum docType;

    private Integer id;

    @Id
    @GeneratedValue(generator = "doc_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "doc_id_seq", sequenceName = "doc_id_seq", allocationSize = 1)
    @Column(name = "DOC_ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToMany(mappedBy = "docId", fetch = FetchType.LAZY ,cascade = {CascadeType.PERSIST, CascadeType.REFRESH}, orphanRemoval = true)
    public List<EventEntity> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<EventEntity> eventIds) {
        this.eventIds = eventIds;
    }

    @Column(name = "PATH_TO_DOC", columnDefinition = "varchar(200)")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "DOC_TYPE", columnDefinition = "varchar(100)")
    public DocEnum getDocType() {
        return docType;
    }

    public void setDocType(DocEnum docType) {
        this.docType = docType;
    }

    //aggiunge le fks e imposta la relazione bidirezionale con lo stesso
    public void addEvent(EventEntity event){
        this.getEventIds().add(event);
        event.setDocId(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentEntity)) return false;

        DocumentEntity that = (DocumentEntity) o;

        if (!path.equals(that.path)) return false;
        return docType == that.docType;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + docType.hashCode();
        return result;
    }
}
