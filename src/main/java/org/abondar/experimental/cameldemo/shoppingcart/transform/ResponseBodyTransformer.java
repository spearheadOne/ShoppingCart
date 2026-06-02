package org.abondar.experimental.cameldemo.shoppingcart.transform;

import org.springframework.stereotype.Component;

@Component
public class ResponseBodyTransformer {


    public String transformBody(Object body){
        byte[] resp = (byte[]) body;
        return new String(resp);
    }

}
