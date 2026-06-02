package org.abondar.experimental.cameldemo.shoppingcart.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.abondar.experimental.cameldemo.shoppingcart.model.CartProduct;
import org.abondar.experimental.cameldemo.shoppingcart.model.CartProductRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ProductProcessor implements Processor {

    private ObjectMapper mapper;

    @Autowired
    public ProductProcessor(ObjectMapper mapper){
        this.mapper = mapper;
    }

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
