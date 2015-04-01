package it.dupps.data;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by dupps on 02.03.15.
 */
@Entity
@Table(name="message")
@Data
public class Message {

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private int messageId;

    @Column
    private String messageText;

    @Column
    private String messageSource;

    @Temporal(TemporalType.TIMESTAMP)
    private Date messageTimestamp;

}
