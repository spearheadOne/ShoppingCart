import cartStore from "../store/cart-store";
import "../stylesheets/Cart.css";

function Cart() {
    const itemsTotal = cartStore((state) => state.cart.itemsTotal);
    const totalPrice = cartStore((state) => state.cart.totalPrice);

    return (
        <div className="cart-summary" aria-label={`${itemsTotal} items in cart`}>
            <span>{itemsTotal} {itemsTotal === 1 ? "item" : "items"}</span>
            <strong>${totalPrice.toFixed(2)}</strong>
        </div>
    );
}

export default Cart;
