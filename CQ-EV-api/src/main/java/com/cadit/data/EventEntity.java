package com.cadit.data;

import javax.persistence.*;

/**
 * *************************************************************************************************
 * ********************************************API*********************************************
 * *************************************************************************************************
 */
@Entity
@Table(name = "EVENT_STORE_DOC" /*, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"DOC_ID","STATUS"})}*/)
/*
}*/
public class EventEntity {

    private Integer id; //PK

    private String infoPath;

    private StatusJob status;

    private DocEnum type;

    private DocumentEntity docId;//FK a EventEntity


    public EventEntity(){

    }

    @Id
    @GeneratedValue(generator = "event_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "event_id_seq", sequenceName = "event_id_seq", allocationSize = 1)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "PATH_INFO", columnDefinition = "varchar(400)")
    public String getInfoPath() {
        return infoPath;
    }

    public void setInfoPath(String infoPath) {
        this.infoPath = infoPath;
    }

    @Column(name = "STATUS", columnDefinition = "varchar(100)")
    @Enumerated(EnumType.STRING)
    public StatusJob getStatus() {
        return status;
    }

    public void setStatus(StatusJob status) {
        this.status = status;
    }


    @Column(name = "DOC_TYPE", columnDefinition = "varchar(400)")
    @Enumerated(EnumType.STRING)
    public DocEnum getType() {
        return type;
    }

    public void setType(DocEnum type) {
        this.type = type;
    }


    @ManyToOne
    @JoinColumn(name = "DOC_FID", referencedColumnName = "DOC_ID",  foreignKey = @ForeignKey(name = "FK_DOC_ID"))
    public DocumentEntity getDocId() {
        return docId;
    }


    public void setDocId(DocumentEntity docId) {
        this.docId = docId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventEntity)) return false;

        EventEntity that = (EventEntity) o;

        if (status != that.status) return false;
        if (type != that.type) return false;
        return docId.equals(that.docId);
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + docId.hashCode();
        return result;
    }
}
