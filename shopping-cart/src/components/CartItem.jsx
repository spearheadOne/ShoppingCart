import React from "react";
import cartStore from "../store/cart-store";

import "../stylesheets/CartItem.css"


function CartItem(props){

    const addItemToCart = cartStore(state => state.addToCart);
    const removeItemFromCart = cartStore(state => state.removeFromCart);
    
    function addToCart(){
        const id = props.id;
        const name = props.name;
        const price = props.price;

        addItemToCart({
            id,
            name,
            price
        });
    }


    function removeFromCart(){
        const id = props.id;
        removeItemFromCart(id);
    }


    return (
        <div className="cart-item">
            <h2>{props.name}</h2>
            <p>${props.price}/-</p>
            <p>x{props.quantity}</p>
            <p>Total ${props.total}</p>
            <button className="cart-item-actions" onClick={removeFromCart}>
                -
            </button>
            <button className="cart-item-actions" onClick={addToCart}>
                +
            </button>
        </div>
    )
}


export default CartItem;
