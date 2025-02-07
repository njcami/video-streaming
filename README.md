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




