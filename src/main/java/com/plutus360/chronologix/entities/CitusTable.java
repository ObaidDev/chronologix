package com.plutus360.chronologix.entities;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Immutable
@Table(name = "citus_tables")
@Subselect("SELECT table_name::text, citus_table_type, distribution_column, colocation_id, table_size, shard_count, table_owner, access_method FROM citus_tables")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitusTable {

    @Id
    @Column(name = "table_name")
    private String tableName;

    @Column(name = "citus_table_type")
    private String citusTableType;

    @Column(name = "distribution_column")
    private String distributionColumn;

    @Column(name = "colocation_id")
    private Integer colocationId;

    @Column(name = "table_size")
    private String tableSize;

    @Column(name = "shard_count")
    private Long shardCount;

    @Column(name = "table_owner")
    private String tableOwner;

    @Column(name = "access_method")
    private String accessMethod;
    
}
