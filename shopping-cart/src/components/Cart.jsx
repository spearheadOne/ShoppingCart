import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { cartActions } from "../store/cart-slice";
import "../stylesheets/Cart.css"

function Cart() {
    const total = useSelector(state => state.cart.itemsTotal); 
    const dispatch = useDispatch();

    function showCart() {
        dispatch(cartActions.setShowCart());
    }

    return (
        <div className="cart">
            <h3 onClick={showCart}> Cart: {total} Items</h3>
        </div>
    )
}

export default Cart;