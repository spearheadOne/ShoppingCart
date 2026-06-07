import React from "react"

import Product from "./Product"
import "../stylesheets/ProductList.css"
import cartStore from "../store/cart-store";

function ProductList() {

     let products = cartStore(state => state.products);
   
     
    const renderedProducts = products.map((product, id) => (
        <li key={id}>
            <Product
                id={product.id}
                name={product.name}
                imgUrl={product.imgURL}
                price={product.price}
            />
        </li>
    ));

    return (
        <div>
            <ul className="product-list">
                {renderedProducts}
            </ul>
        </div>
    )
}

export default ProductList;
