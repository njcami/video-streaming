Video Streaming API

This repository contains a simple video streaming API that allows users to upload videos and stream them. The API is
built using Java 21 LTS and Spring Boot 3.4.2 . It uses JWT for authentication. It requires a Docker environment
(server version 1.6.0 or higher) since it uses TestContainers to run for both the default and test profiles. No other
database is required on the host system.

When run locally the API documentation can be accessed at http://localhost:8080/swagger-ui/index.html and has the
following user endpoints:

Register a new user:

curl -X POST http://localhost:8080/auth/register \
-H "Content-Type: application/json" \
-d '{
"email": "newuser@example.com",
"password": "securepassword"
}'

Login with the new user:

curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{
"email": "newuser@example.com",
"password": "securepassword"
}'

Logout:
curl -X POST http://localhost:8080/auth/logout \
-H "Content-Type: application/json" \
-d '{
"token": "your-jwt-token-here"
}'

Once registered and logged in, the users can:

    Users with Role VIEWER can do GET operations on videos endpoints. 
    Users with Role UPLOADER can also do POST and PUT operations on videos endpoints.
    Users with Role ADMIN can also do a DELETE operations on the videos endpoint.




