package com.plutus360.chronologix.entities;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Immutable
@Subselect("SELECT shardid, table_name, shard_name, citus_table_type, colocation_id, nodename, nodeport, shard_size FROM citus_shards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShardInfo {

    @Id
    @Column(name = "shardid")
    private Long shardId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "shard_name")
    private String shardName;

    @Column(name = "citus_table_type")
    private String citusTableType;

    @Column(name = "colocation_id")
    private Integer colocationId;

    @Column(name = "nodename")
    private String nodeName;

    @Column(name = "nodeport")
    private Integer nodePort;

    @Column(name = "shard_size")
    private Long shardSize;
    
}
