package com.plutus360.chronologix.utils;




import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.exception.UnableToProccessIteamException;
import com.plutus360.chronologix.service.IntegrationTokenService;

import lombok.Data;



@Data
@Component
public class ACLManager {


    private IntegrationTokenService integrationTokenService;

    @Autowired
    ACLManager(IntegrationTokenService integrationTokenService) {
        this.integrationTokenService = integrationTokenService;
    }



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




    /**
     * utility method to check if the token is valid
     * @param token
     * @param uri
     * @param method
     */

    public void checkAcess(String token ,String uri , String method) {

        if (token == null || token.isEmpty()) {
            throw new UnableToProccessIteamException("Token cannot be null or empty");
        }

        IntegrationToken integrationToken = integrationTokenService.findBytoken(token)
        .orElseThrow(() -> new UnableToProccessIteamException("Token not found: " + token));

        // Check expiration
        if (integrationToken.getExpiredAt() != null && 
            integrationToken.getExpiredAt().isBefore(OffsetDateTime.now())) {
            throw new UnableToProccessIteamException("Token has expired");
        }

        Map<String, Object> tokenInfo = integrationToken.getTokenInfo();


        // Check access permissions
        if (!hasAccess(getAclTable(tokenInfo), uri, method, null)) {
            throw new UnableToProccessIteamException("Access denied for token " + token);
        }

    }



    /**
     * utility method to check if the token is valid
     * @param token
     * @param uri
     * @param method
     */
    public Map<String, Map<String, Set<String>>> getAclTable (Map<String, Object> tokenInfo) {

        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<String>>> rawAcl = (Map<String, Map<String, List<String>>>) (Map<?, ?>) tokenInfo;

        return convertToSet(rawAcl);
    }
    
}
