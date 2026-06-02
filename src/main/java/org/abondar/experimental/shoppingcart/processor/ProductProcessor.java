package org.abondar.experimental.shoppingcart.processor;

import lombok.AllArgsConstructor;
import org.abondar.experimental.shoppingcart.model.CartProduct;
import org.abondar.experimental.shoppingcart.model.CartProductRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultMessage;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ProductProcessor implements Processor {

    private final ObjectMapper mapper;


    @Override
    public void process(Exchange exchange) throws Exception {
        var body =   exchange.getIn().getBody();
        var productRequest = (CartProductRequest) body;

        Map<String, CartProduct> productMap = new HashMap<>();

        productRequest.products().forEach(cartProduct -> {
            String id = UUID.randomUUID().toString();
            productMap.put(id,cartProduct);
        });

        var productBody= mapper.writeValueAsString(productMap);

        Message msg = new DefaultMessage(exchange);
        msg.setBody(productBody);
        exchange.setMessage(msg);
    }
}
