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

## Login with the New User
```sh
curl -X POST "http://localhost:8080/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
           "email": "newuser@example.com",
           "password": "securepassword"
         }'
```

## Logout
```sh
curl -X POST "http://localhost:8080/auth/logout" \
     -H "Content-Type: application/json" \
     -d '{
           "token": "your-jwt-token-here"
         }'
```

Once registered and logged in, the users can:

- **Users with Role VIEWER**: Can perform GET operations on videos endpoints.
- **Users with Role UPLOADER**: Can perform POST and PUT operations on videos endpoints.
- **Users with Role ADMIN**: Can perform DELETE operations on the videos endpoint.

## VideoController Endpoints

### Publish a Video
```sh
curl -X POST "http://localhost:8080/videos" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/video.mp4" \
     -F 'videoMetaDataDTO={ "title": "Sample Video", "synopsis": "A sample video synopsis", "directorName": "Director Name", "mainActor": "Main Actor", "cast": ["Actor 1", "Actor 2"], "yearOfRelease": 2022, "genre": ["ACTION"], "runningTime": 120 }'
```

### Update Video Metadata
```sh
curl -X PUT "http://localhost:8080/videos/1" \
     -H "Content-Type: application/json" \
     -d '{ "title": "Updated Title", "synopsis": "Updated synopsis", "directorName": "Updated Director", "mainActor": "Updated Main Actor", "cast": ["Actor 1", "Actor 2"], "yearOfRelease": 2022, "genre": ["DRAMA"], "runningTime": 130 }'
```

### Play Video
```sh
curl -X GET "http://localhost:8080/videos/play/1"
```

### Get Video Metadata
```sh
curl -X GET "http://localhost:8080/videos/1"
```

### Delete Video
```sh
curl -X DELETE "http://localhost:8080/videos/1"
```

### Find All Videos
```sh
curl -X GET "http://localhost:8080/videos"
```

### Find All Video Impressions
```sh
curl -X GET "http://localhost:8080/videos/1/impressions"
```

### Find All Video Views
```sh
curl -X GET "http://localhost:8080/videos/1/views"
```

### Search Videos by Title
```sh
curl -X GET "http://localhost:8080/videos/search/title?title=Sample Title"
```

### Search Videos by Director
```sh
curl -X GET "http://localhost:8080/videos/search/director?director=Sample Director"
```

### Search Videos by Main Actor
```sh
curl -X GET "http://localhost:8080/videos/search/mainActor?mainActor=Sample Actor"
```

### Search Videos by Running Time
```sh
curl -X GET "http://localhost:8080/videos/search/runningTime?runningTime=120&comparator=GREATER_OR_EQUAL"
```

### Search Videos by Genre
```sh
curl -X GET "http://localhost:8080/videos/search/genre?genre=ACTION"
```
