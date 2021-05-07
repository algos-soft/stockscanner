# StockScanner

Market investment simulator

## Running and debugging the application

### Running the application from the command line.
To run from the command line, use `mvn` and open http://localhost:8080 in your browser.

## Project structure

- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `frontend/` contains the client-side JavaScript views of your application.

## Enabling Production Mode

- in application.properties:
make sure you have this line in application.properties:
    spring.profiles.active=prod

deploy directory structure
[stockscanner]
    -- application.jar
    [config]
        -- application.properties 
        -- ...

## Build for production
> mvn clean package -Pproduction

(the 'production' profile is defined in pom.xml)

a .jar file is created in the 'target' directory

## Set back to debug mode
After a Production build, if the project still builds in production mode even if you set the profution flag to false, you have to clean the project:
> mvn clean

