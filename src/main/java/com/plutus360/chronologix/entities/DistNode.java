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
@Subselect("SELECT nodeid, groupid, nodename, nodeport, noderack, hasmetadata, isactive, noderole, nodecluster, metadatasynced, shouldhaveshards FROM pg_dist_node")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistNode {

    @Id
    @Column(name = "nodeid")
    private Long nodeId;

    @Column(name = "groupid")
    private Integer groupId;

    @Column(name = "nodename")
    private String nodeName;

    @Column(name = "nodeport")
    private Integer nodePort;

    @Column(name = "noderack")
    private String nodeRack;

    @Column(name = "hasmetadata")
    private Boolean hasMetadata;

    @Column(name = "isactive")
    private Boolean isActive;

    @Column(name = "noderole")
    private String nodeRole;

    @Column(name = "nodecluster")
    private String nodeCluster;

    @Column(name = "metadatasynced")
    private Boolean metadataSynced;

    @Column(name = "shouldhaveshards")
    private Boolean shouldHaveShards;
    
}
