import React from "react";
import cartStore from "../store/cart-store";

import "../stylesheets/Product.css"

function Product(props) {

    const addItemToCart = cartStore(state => state.addToCart);
    
    function addToCart() {
        const name = props.name;
        const id = props.id;
        const price = props.price

        addItemToCart({
            name,
            id,
            price
        })
         
    }


    return (
        <div className="product-card">
            <img src={props.imgUrl} alt={props.name} />
            <h2>{props.name}</h2>
            <p>$ {props.price}</p>
            <button className="product-button" onClick={addToCart}>Add to cart</button>
        </div>
    )
}

export default Product;
