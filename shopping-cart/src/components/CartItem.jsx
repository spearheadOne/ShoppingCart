import React from "react";
import { useDispatch } from "react-redux";
import { cartActions } from "../store/cart-slice";

import "../stylesheets/CartItem.css"


function CartItem(props){

    const dispatch = useDispatch();
    
    function addToCart(){
        const id = props.id;
        const name = props.name;
        const price = props.price;

        dispatch(
            cartActions.addToCart({
                id,
                name,
                price
            })
        );
    }


    function removeFromCart(){
        const id = props.id;
        dispatch(
            cartActions.removeFromCart({id})
        );
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