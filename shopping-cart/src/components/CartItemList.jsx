import React from "react";

import CartItem from "./CartItem";
import "../stylesheets/CartItemList.css"
import cartStore from "../store/cart-store";

function CartItemList() {

    let cartItems = cartStore(state => state.cart.items);
    
    cartItems = cartItems.map(item => (
        <li className="cart-item-li"  key={item.id}>
            <CartItem
                id={item.id}
                price={item.price}
                total={item.totalPrice}
                name={item.name}
                quantity={item.quantity} />
        </li>

    ));


    return (
        <div className="cart-item-list">
            <h2>Your cart</h2>
            <ul className="cart-item-ul">
                {cartItems}
            </ul>
        </div>

    )
}


export default CartItemList;
