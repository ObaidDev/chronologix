
| Feature / Aspect        | OAuth 2.0                  | OpenID Connect                 | SAML                        |
|-------------------------|----------------------------|--------------------------------|-----------------------------|
| Protocol Type           | Authorization framework    | Authentication layer over OAuth2 | Authentication protocol    |
| Main Purpose            | Access control to APIs     | Identity + access control      | Identity (SSO)              |
| Token Format            | Access token (usually JWT) | Access + ID token (JWT)        | SAML Assertion (XML)        |
| Identity Info Provided? | ❌ No                      | ✅ Yes                         | ✅ Yes                      |
| Common Use Case         | API access from apps       | Login with social identity     | Enterprise SSO (legacy apps)|
| Transport               | REST (JSON)                | REST (JSON)                    | Browser redirects (XML)     |
| Modern?                 | ✅ Yes                     | ✅ Yes                         | ❌ Older/legacy             |


https://medium.com/@tericcabrel/implement-jwt-authentication-in-a-spring-boot-3-application-5839e4fd8fac
