# ShoppingCart

Multi-project shopping cart consisting of two Spring Boot services and one standalone Express UI.

## Structure

```text
ShoppingCart/
├── cart-service/
│   ├── build.gradle
│   ├── shopping-cart/       # Cart React/Vite UI
│   └── src/                 # Cart Spring Boot service
├── product-service/
│   ├── build.gradle
│   ├── product-ui/          # Product React/Vite UI
│   └── src/                 # Product Spring Boot service, H2 and Liquibase
├── combined-ui/             # Standalone Express composition server
├── shared/                  # Shared framework-free Java utilities
├── build.gradle             # Aggregation only
└── settings.gradle
```

The cart service retrieves product details through the product service HTTP API. The combined UI
only embeds and coordinates the two UIs; it is not packaged into or served by either backend.

## Install frontend dependencies

```shell
cd cart-service/shopping-cart && npm install
cd ../../product-service/product-ui && npm install
cd ../../combined-ui && npm install
```

## Build and test

All Gradle modules:

```shell
./gradlew clean test build
```

Frontend applications:

```shell
cd cart-service/shopping-cart && npm test && npm run build
cd product-service/product-ui && npm test && npm run build
cd combined-ui && npm test
```

The cart and product service JAR tasks build and embed only their respective Vite applications.

## Run locally

Start Redis:

```shell
docker compose up -d redis
```

Run each application in a separate terminal:

```shell
./gradlew :product-service:bootRun
./gradlew :cart-service:bootRun
cd combined-ui && npm start
```

Open:

- Combined UI: http://localhost:3000
- Cart UI: http://localhost:8080
- Product UI: http://localhost:8081
- Cart API reference: http://localhost:8080/swagger-ui/index.html
- Product API reference: http://localhost:8081/swagger-ui/index.html
- Cart actuator: http://localhost:8080/actuator
- Product actuator: http://localhost:8081/actuator
- Combined UI health: http://localhost:3000/health

Configuration:

- `PRODUCT_SERVICE_URL`: product API base URL used by the cart service.
- `PORT`: combined UI server port, default `3000`.
- `CART_UI_URL`: cart UI URL embedded by the combined UI, default `http://localhost:8080`.
- `PRODUCT_UI_URL`: product UI URL embedded by the combined UI, default `http://localhost:8081`.

## Container images

```shell
./gradlew :cart-service:bootBuildImage :product-service:bootBuildImage
docker build -t abondar/shopping-cart-combined-ui combined-ui
```

This creates:

- `abondar/shopping-cart`
- `abondar/product-service`
- `abondar/shopping-cart-combined-ui`

Run the combined UI container:

```shell
docker run --rm -p 3000:3000 \
  -e CART_UI_URL=http://localhost:8080 \
  -e PRODUCT_UI_URL=http://localhost:8081 \
  abondar/shopping-cart-combined-ui
```
