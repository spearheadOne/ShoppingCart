import React from "react";
import cartStore from "../store/cart-store";
import "../stylesheets/Cart.css"

function Cart() {
    const total = cartStore(state => state.cart.itemsTotal);
    const toggleCart = cartStore(state => state.toggleCart);

    function showCart() {
        toggleCart();
    }

    return (
        <div className="cart">
            <h3 onClick={showCart}> Cart: {total} Items</h3>
        </div>
    )
}

export default Cart;
