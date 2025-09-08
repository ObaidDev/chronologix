package com.plutus360.chronologix.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plutus360.chronologix.dao.repositories.DatabaseInfoRepo;
import com.plutus360.chronologix.entities.CitusTable;
import com.plutus360.chronologix.entities.DistNode;
import com.plutus360.chronologix.entities.ShardInfo;




@RestController
@RequestMapping("/db-info")
public class DatabaseInfoController {

    private final DatabaseInfoRepo shardInfoRepository;

    
    @Autowired
    public DatabaseInfoController(DatabaseInfoRepo shardInfoRepository) {
        this.shardInfoRepository = shardInfoRepository;
    }

    @GetMapping("/shards")
    public List<ShardInfo> getAllShards() {
        return shardInfoRepository.getAllShards();
    }


    @GetMapping("/nodes")
    public List<DistNode> getAllDistNodes() {
        return shardInfoRepository.getAllDistNodes();
    }

    @GetMapping("/tables")
    public List<CitusTable> getAllCitusTables() {
        return shardInfoRepository.getAllCitusTables();
    }
}
