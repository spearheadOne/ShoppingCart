import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { cartActions } from "../store/cart-slice";

import "../stylesheets/Product.css"

function Product(props) {

    const dispatch = useDispatch();
    const items = useSelector((state)=> state.cart.items);
    
    function addToCart() {
        const name = props.name;
        const id = props.id;
        const price = props.price

        dispatch(
            cartActions.addToCart({
                name,
                id,
                price
            })
        )
         
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