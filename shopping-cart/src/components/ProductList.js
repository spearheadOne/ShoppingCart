import React from "react"

import Product from "./Product"
import "../stylesheets/ProductList.css"
import { useSelector } from "react-redux";

function ProductList() {

     let products = useSelector(state => state.product.products);
   
     
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