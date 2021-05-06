# StockScanner

Market investment simulator

## Running and debugging the application

### Running the application from the command line.
To run from the command line, use `mvn` and open http://localhost:8080 in your browser.

## Project structure

- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `frontend/` contains the client-side JavaScript views of your application.

## Build for production
mvn clean package -Pproduction

a .jar file is created in the 'target' directory

make sure you have this line in application.properties:
    spring.profiles.active=prod

deploy directory structure
[stockscanner]
    -- application.jar
    [config]
        -- application.properties 
        -- ...
     
