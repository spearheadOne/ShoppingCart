# ShoppingCart


Camel + Spring Boot based [shopping cart react-app](https://github.com/abondar24/ReactDemo/tree/master/shopping-cart) backend.

## Build,test and run

- Build the app using gradle wrapper
```
./gradlew clean build
```

- Build docker image
```
./gradlew bootBuildImage
```

### Test
```
./gradlew clean test
```

### Run
- Run the app using gradlew
```
./gradlew bootRun
```

- Run the app via jar
```
java -jar build/libs/ShoppingCart-1.0-SNAPSHOT.jar
```

- Run a docker image
```
docker run -d --name todo  -p 8080:8080 abondar/shoppingCart
```

## Endpoints
- API reference: http://localhost:8080/swagger-ui/index.html
- Actuator: http://localhost:8080/actuator

## Note

- Change create log directory as mentioned in logback.xml file
- Add exclusions to corresponding tasks in gradle when you add a new test class