package com.plutus360.chronologix.utils;




import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
@Component
public class ACLManager {



    public Map<String, Map<String, Set<String>>> convertToSet(Map<String, Map<String, List<String>>> rawAcl) {
        Map<String, Map<String, Set<String>>> optimizedAcl = new HashMap<>();
        for (var entry : rawAcl.entrySet()) {
            String uri = entry.getKey();
            Map<String, List<String>> permissions = entry.getValue();

            Map<String, Set<String>> optimizedPermissions = new HashMap<>();
            optimizedPermissions.put("methods", new HashSet<>(permissions.getOrDefault("methods", List.of())));
            optimizedPermissions.put("ids", new HashSet<>(permissions.getOrDefault("ids", List.of())));

            optimizedAcl.put(uri, optimizedPermissions);
        }
        return optimizedAcl;
    }




    public boolean hasAccess(Map<String, Map<String, Set<String>>> aclTable, String uri, String method, List<String> itemIds) {
        
        if (!aclTable.containsKey(uri)) {
            return false; // URI not found
        }

        Map<String, Set<String>> permissions = aclTable.get(uri);
        Set<String> allowedMethods = permissions.getOrDefault("methods", Set.of());

        if (!allowedMethods.contains(method)) {
            return false; // Method not allowed
        }

        Set<String> allowedIds = permissions.getOrDefault("ids", Set.of());

        // If no specific IDs are enforced, allow access
        if (allowedIds.isEmpty()) {
            return true;
        }

        // ✅ More Efficient Check Using `Set.containsAll()`
        return allowedIds.containsAll(itemIds);
    }
    
}
