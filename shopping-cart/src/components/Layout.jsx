import React from "react";
import Header from "./Header"
import ProductList from "./ProductList";
import CartItemList from "./CartItemList"
import cartStore from "../store/cart-store";

import "../stylesheets/Layout.css"

function Layout() {
    let total = 0;
    const items = cartStore(state => state.cart.items);

    items.forEach(item => {
        total += item.totalPrice;
    })

    const showCart = cartStore(state => state.cart.showCart);

    return (
        <React.Fragment>
            <div className="layout">
                <Header />
                <ProductList />
                {showCart && <CartItemList />}
                <div className="layout-price">
                    <h3>Total: ${total}</h3>
                    <button className="layout-order">Order</button>
                </div>

            </div>
        </React.Fragment>

    )
}


export default Layout;
