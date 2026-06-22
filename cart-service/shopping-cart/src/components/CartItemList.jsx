import { useState } from "react";
import CartItem from "./CartItem";
import cartStore from "../store/cart-store";
import "../stylesheets/CartItemList.css";

function CartItemList() {
    const [confirmingDelete, setConfirmingDelete] = useState(false);
    const cart = cartStore((state) => state.cart);
    const cartLoading = cartStore((state) => state.cartLoading);
    const deletingCart = cartStore((state) => state.deletingCart);
    const deleteCart = cartStore((state) => state.deleteCart);
    const fetchCartData = cartStore((state) => state.fetchCartData);

    return (
        <div className="cart-item-list">
            {cart.id && (
            <div className="cart-title-row">
                {cart.id && !confirmingDelete && (
                    <div className="cart-actions">
                        <button
                            className="refresh-cart-button"
                            onClick={fetchCartData}
                            disabled={cartLoading || deletingCart}
                        >
                            {cartLoading ? "Refreshing…" : "Refresh"}
                        </button>
                        <button
                            className="delete-cart-button"
                            onClick={() => setConfirmingDelete(true)}
                            disabled={deletingCart}
                        >
                            Delete cart
                        </button>
                    </div>
                )}
                {cart.id && confirmingDelete && (
                    <div className="delete-cart-confirmation" role="group" aria-label="Confirm cart deletion">
                        <span>Delete this cart?</span>
                        <button
                            className="confirm-delete-button"
                            onClick={async () => {
                                await deleteCart();
                                setConfirmingDelete(false);
                            }}
                            disabled={deletingCart}
                        >
                            {deletingCart ? "Deleting…" : "Yes, delete"}
                        </button>
                        <button
                            className="cancel-delete-button"
                            onClick={() => setConfirmingDelete(false)}
                            disabled={deletingCart}
                        >
                            Cancel
                        </button>
                    </div>
                )}
            </div>
            )}

            {cartLoading ? (
                <div className="cart-state">Loading cart…</div>
            ) : !cart.id ? (
                <div className="cart-state">
                    <h3>No active cart</h3>
                    <p>Add a product to create a cart.</p>
                </div>
            ) : cart.items.length === 0 ? (
                <div className="cart-state empty-cart">
                    <span aria-hidden="true">＋</span>
                    <h3>Your cart is empty</h3>
                    <p>Choose a product from the catalog to get started.</p>
                </div>
            ) : (
                <ul className="cart-item-ul">
                    {cart.items.map((item) => (
                        <li key={item.id}>
                            <CartItem {...item} />
                        </li>
                    ))}
                </ul>
            )}

            <div className="cart-totals">
                <div>
                    <span>Items</span>
                    <strong>{cart.itemsTotal}</strong>
                </div>
                <div className="grand-total">
                    <span>Cart total</span>
                    <strong>${cart.totalPrice.toFixed(2)}</strong>
                </div>
            </div>
        </div>
    );
}

export default CartItemList;
