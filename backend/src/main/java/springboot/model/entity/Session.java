package com.spring.boot.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "session")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Session implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "com.spring.boot.util.IDGeneratorUtil")
    @Column(name = "token_id", nullable = false, length = 36)
    private String tokenId;
    private String accountLoginId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;
    @CreationTimestamp
    @Column(nullable = false)
    private Date loginDate;
    private String sessionData;
}
