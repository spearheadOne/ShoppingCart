import { createSlice } from "@reduxjs/toolkit";


const productSlice = createSlice({
    name: 'product',
    initialState: {
        products: []
    },
    reducers: {
        replaceData(state,action) {
            let rawProducts = action.payload;
            let products = []
            for (var index in rawProducts){
              
                let product = {}
                product.id = index
                product.name = rawProducts[index].name
                product.imgURL = rawProducts[index].imgUrl
                product.price = rawProducts[index].price
                
                products.push(product)
            }
            state.products = products;
        }
    }
  
});

export const productActions = productSlice.actions

export default productSlice