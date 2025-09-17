package com.plutus360.chronologix.tests.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plutus360.chronologix.entities.IntegrationToken;
import com.plutus360.chronologix.exception.UnableToProccessIteamException;
import com.plutus360.chronologix.service.IntegrationTokenService;
import com.plutus360.chronologix.utils.CustomACLManager;
import com.trackswiftly.utils.base.services.CompressedAclService;


@ExtendWith(MockitoExtension.class)
class CustomACLManagerTest {


    @Mock
    private IntegrationTokenService integrationTokenService;

    @InjectMocks
    private CustomACLManager customACLManager;

    private IntegrationToken validToken;
    private String testToken;
    private String testUri;
    private String testMethod;


    @BeforeEach
    void setUp() {
        testToken = "test-token-123";
        testUri = "/api/v1/users";
        testMethod = "GET";
        
        validToken = IntegrationToken.builder()
            .id(1L)
            .userId("user123")
            .tokenHash("hashed-token-value")
            .name("Test API Token")
            .tokenIndexed("compressed-acl-data")
            .active(true)
            .expiredAt(OffsetDateTime.now().plusDays(1)) // Valid token
            .createdAt(OffsetDateTime.now().minusDays(7))
            .updatedAt(OffsetDateTime.now())
            .build();
    }


    @Test
    void checkAccessShouldThrowExceptionWhenTokenIsExpired() {

        IntegrationToken expiredToken = IntegrationToken.builder()
            .id(2L)
            .userId("user456")
            .tokenHash("expired-token-hash")
            .name("Expired API Token")
            .tokenIndexed("compressed-acl-data")
            .active(true)
            .expiredAt(OffsetDateTime.now().minusDays(1)) // Expired token
            .createdAt(OffsetDateTime.now().minusDays(7))
            .updatedAt(OffsetDateTime.now())
            .build();
        
        when(integrationTokenService.findBytoken(testToken))
            .thenReturn(Optional.of(expiredToken));

        // Act & Assert
        UnableToProccessIteamException exception = assertThrows(
            UnableToProccessIteamException.class,
            () -> customACLManager.checkAcess(testToken, testUri, testMethod)
        );
        
        assertEquals("Token has expired", exception.getMessage());
        verify(integrationTokenService).findBytoken(testToken);
    }



    @Test
    void checkAccess_ShouldSucceed_WhenAllValidationsPass() {
        // Arrange
        Map<String, Map<String, Set<String>>> aclMap = createMockAclMap();
        
        when(integrationTokenService.findBytoken(testToken))
            .thenReturn(Optional.of(validToken));

        try (MockedStatic<CompressedAclService> mockedService = mockStatic(CompressedAclService.class)) {
            mockedService.when(() -> CompressedAclService.decompressAcl("compressed-acl-data"))
                .thenReturn(aclMap);
            
            // Create a spy to mock the hasAccess method
            CustomACLManager spyManager = spy(customACLManager);
            doReturn(true).when(spyManager).hasAccess(aclMap, testUri, testMethod, null);

            // Act & Assert - Should not throw exception
            assertDoesNotThrow(() -> spyManager.checkAcess(testToken, testUri, testMethod));
        }
        
        verify(integrationTokenService).findBytoken(testToken);
    }





    /**
     * Helper method to create a mock ACL map for testing
     */
    private Map<String, Map<String, Set<String>>> createMockAclMap() {
        Map<String, Map<String, Set<String>>> aclMap = new HashMap<>();
        Map<String, Set<String>> resourceMap = new HashMap<>();
        resourceMap.put(testUri, Set.of(testMethod));
        aclMap.put("permissions", resourceMap);
        return aclMap;
    }

    
    @Test
    void testCheckAcess() {

    }
}
