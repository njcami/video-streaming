# Video Streaming API

This repository contains a simple video streaming API that allows users to upload videos and play them. 

This service is built using Java 21 LTS and Spring Boot 3.4.2. It uses JWT for authentication. It requires a Docker environment 
(server version 1.6.0 or higher) since it uses TestContainers with MySQL database to run for both the default and test profiles. 
No other database is required on the host system.

## Building and Running the Project

To build the project, make sure to have Java 21 LTS installed and run the following command:

```sh
mvn clean install
```

To run the project, execute the following command:

```sh
mvn spring-boot:run
```

By default the application will be available at [http://localhost:8080](http://localhost:8080).


## Practical Assumptions

The following assumptions that were made:
- The video files are stored in the local file system.
- The video files are stored in the same directory as the application.
- When a video file and the metadata are uploaded (one API call to reduce orphan files/metadata), the video file is stored in the local file system and the metadata is stored in the MySQL database.
- A user can be registered, logged in and out from the OpenAPI documentation (No other user interface is required, but can be used).
- A video file can be published, updated, played, and deleted from the OpenAPI documentation.
- Video searches can also be done from the OpenAPI documentation.
- Maximum video file size is set to 1024MB.
- Videos are not deleted from the local file system when metadata is soft-deleted from the database (made active=false).
- No Spring profile is used when running the application. The default profile is used.
- During test, the Spring profile is automatically set to test. Both unit and also integration tests are run.
- During build, the test phase is invoked in the Maven Lifecycle therefore all tests are run.

To use a different database, change the application.properties file in the resources folder. The default database is MySQL.

## API Documentation

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

### Publish a Video (MP4 file)
```sh
curl -X POST "http://localhost:8080/videos" \
     -H 'accept: */*' \
     -H "Authorization: Bearer your-jwt-token-here" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/video.mp4;type=video/mp4" \
     -F 'videoMetaDataDTO={"title": "Sample Video", "synopsis": "A sample video synopsis", "directorName": "Director Name", "mainActor": "Main Actor", "cast": [], "yearOfRelease": 2022, "genre": ["ACTION"], "runningTime": 120, "fileExtension": "mp4", "fileName": "video.mp4"}' \
     -F 'videoMetaDataDTO=@metadata.json;type=application/json'     `
```

### Update Video Metadata
```sh
curl -X PUT "http://localhost:8080/videos/1" \
     -H "Authorization: Bearer your-jwt-token-here" \
     -H "Content-Type: application/json" \
     -d '{ "title": "Updated Title", "synopsis": "Updated synopsis", "directorName": "Updated Director", "mainActor": "Updated Main Actor", "cast": [], "yearOfRelease": 2022, "genre": ["DRAMA"], "runningTime": 130, "fileExtension": "mp4", "fileName": "video.mp4" }'
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
