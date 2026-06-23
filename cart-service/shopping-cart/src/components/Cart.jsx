import cartStore from "../store/cart-store";
import "../stylesheets/Cart.css";

function Cart() {
    const cartId = cartStore((state) => state.cart.id);
    const items = cartStore((state) => state.cart.items);
    const itemsTotal = cartStore((state) => state.cart.itemsTotal);
    const totalPrice = cartStore((state) => state.cart.totalPrice);
    const cartLoading = cartStore((state) => state.cartLoading);
    const deletingCart = cartStore((state) => state.deletingCart);
    const submittingCart = cartStore((state) => state.submittingCart);
    const submitCart = cartStore((state) => state.submitCart);

    const canSubmit = Boolean(cartId) && items.length > 0;

    return (
        <div className="cart-summary" aria-label={`${itemsTotal} items in cart`}>
            <span>{itemsTotal} {itemsTotal === 1 ? "item" : "items"}</span>
            <strong>${totalPrice.toFixed(2)}</strong>
            <button
                className="submit-cart-button"
                onClick={submitCart}
                disabled={!canSubmit || cartLoading || deletingCart || submittingCart}
            >
                {submittingCart ? "Submitting..." : "Submit"}
            </button>
        </div>
    );
}

export default Cart;
