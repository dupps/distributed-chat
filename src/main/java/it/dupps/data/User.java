package it.dupps.data;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by dupps on 13.04.15.
 */

@Entity
@Table(name="user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int userId;

    @Column
    private String email;

    @Column
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}
