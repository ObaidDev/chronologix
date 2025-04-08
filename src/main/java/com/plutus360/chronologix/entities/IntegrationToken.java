package com.plutus360.chronologix.entities;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "integration_tokens")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationToken {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "integration_tokens_id_seq")
    @SequenceGenerator(name = "integration_tokens_id_seq", sequenceName = "integration_tokens_id_seq", allocationSize = 2)
    private Long id;

    @Column(name = "user_id", nullable = false , length = 64)
    private String userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "token_info", columnDefinition = "jsonb")
    private Map<String, Object> tokenInfo;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", columnDefinition = "timestamp(6) with time zone")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamp(6) with time zone")
    private OffsetDateTime updatedAt;

    
}
