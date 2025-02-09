# Video Streaming API

This repository contains a simple video streaming API that allows users to upload videos and stream them. The API is built using Java 21 LTS and Spring Boot 3.4.2. It uses JWT for authentication. It requires a Docker environment (server version 1.6.0 or higher) since it uses TestContainers to run for both the default and test profiles. No other database is required on the host system.

When run locally, the API documentation can be accessed at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) and has the following user endpoints:

## Register a New User
```sh
curl -X POST "http://localhost:8080/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
           "email": "newuser@example.com",
           "password": "securepassword"
         }'
```

## Login to Get JWT Bearer Token
```sh
curl -X POST "http://localhost:8080/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
           "email": "newuser@example.com",
           "password": "securepassword"
         }'
```

## Logout with JWT Bearer Token
```sh
curl -X POST "http://localhost:8080/auth/logout" \
     -H "Content-Type: application/json" \
     -d '{
           "token": "your-jwt-token-here"
         }'
```

Once registered (default user role is ADMIN for simplicity to try all endpoints) and logged in, the users can:

- **Users with Role VIEWER**: Can perform GET operations on videos endpoints.
- **Users with Role CREATOR**: Like VIEWER but can also perform POST and PUT operations on videos endpoints.
- **Users with Role ADMIN**: Like CREATOR but can also perform DELETE operations on the videos endpoint.

After registration and login put the JWT token text field which appears when the upper 
left Authorize labelled button is pressed. The following endpoints all require the bearer
token to be in place.

## VideoController Endpoints

### Publish a Video
```sh
curl -X POST "http://localhost:8080/videos" \
     -H "Authorization: Bearer your-jwt-token-here" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/video.mp4" \
     -F 'videoMetaDataDTO={ "title": "Sample Video", "synopsis": "A sample video synopsis", "directorName": "Director Name", "mainActor": "Main Actor", "cast": ["Actor 1", "Actor 2"], "yearOfRelease": 2022, "genre": ["ACTION"], "runningTime": 120 }'
```

### Update Video Metadata
```sh
curl -X PUT "http://localhost:8080/videos/1" \
     -H "Authorization: Bearer your-jwt-token-here" \
     -H "Content-Type: application/json" \
     -d '{ "title": "Updated Title", "synopsis": "Updated synopsis", "directorName": "Updated Director", "mainActor": "Updated Main Actor", "cast": ["Actor 1", "Actor 2"], "yearOfRelease": 2022, "genre": ["DRAMA"], "runningTime": 130 }'
```

### Play Video
```sh
curl -X GET "http://localhost:8080/videos/play/1" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Get Video Metadata
```sh
curl -X GET "http://localhost:8080/videos/1" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Delete Video
```sh
curl -X DELETE "http://localhost:8080/videos/1" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Find All Videos
```sh
curl -X GET "http://localhost:8080/videos" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Find All Video Impressions
```sh
curl -X GET "http://localhost:8080/videos/1/impressions" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Find All Video Views
```sh
curl -X GET "http://localhost:8080/videos/1/views" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Search Videos by Title
```sh
curl -X GET "http://localhost:8080/videos/search/title?title=Sample Title" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Search Videos by Director
```sh
curl -X GET "http://localhost:8080/videos/search/director?director=Sample Director" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Search Videos by Main Actor
```sh
curl -X GET "http://localhost:8080/videos/search/mainActor?mainActor=Sample Actor" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Search Videos by Running Time
```sh
curl -X GET "http://localhost:8080/videos/search/runningTime?runningTime=120&comparator=GREATER_OR_EQUAL" \
     -H "Authorization: Bearer your-jwt-token-here"
```

### Search Videos by Genre
```sh
curl -X GET "http://localhost:8080/videos/search/genre?genre=ACTION" \
     -H "Authorization: Bearer your-jwt-token-here"
```
