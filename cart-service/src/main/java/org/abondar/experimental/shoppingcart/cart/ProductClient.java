package org.abondar.experimental.shoppingcart.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class ProductClient {

    private final RestClient.Builder restClientBuilder;
    private final String productServiceBaseUrl;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.product.base-url}") String productServiceBaseUrl
    ) {
        this.restClientBuilder = restClientBuilder;
        this.productServiceBaseUrl = productServiceBaseUrl;
    }

    public ProductCatalogItem getProduct(UUID productId) {
        try {
            var product = restClientBuilder
                    .baseUrl(productServiceBaseUrl)
                    .build()
                    .get()
                    .uri("/v1/products/{id}", productId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ProductNotFoundException(productId);
                    })
                    .body(ProductCatalogItem.class);

            if (product == null) {
                throw new ProductServiceException("Product service returned an empty response");
            }
            return product;
        } catch (ProductNotFoundException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ProductServiceException("Product service is unavailable", exception);
        }
    }
}
