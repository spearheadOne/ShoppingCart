import cartStore from "../store/cart-store";
import "../stylesheets/Product.css";

function Product({ id, name, imgUrl, price }) {
    const addToCart = cartStore((state) => state.addToCart);
    const pending = cartStore((state) => Boolean(state.pendingItems[id]));
    const quantity = cartStore((state) => (
        state.cart.items.find((item) => item.id === id)?.quantity || 0
    ));

    return (
        <article className="product-card">
            <div className="product-image-wrap">
                <img src={imgUrl} alt="" />
                {quantity > 0 && <span className="in-cart-badge">{quantity} in cart</span>}
            </div>
            <div className="product-info">
                <h2>{name}</h2>
                <p>${price.toFixed(2)}</p>
            </div>
            <button
                className="product-button"
                onClick={() => addToCart({ id, name, price, imgURL: imgUrl })}
                disabled={pending}
            >
                {pending ? "Adding…" : quantity > 0 ? "Add another" : "Add to cart"}
            </button>
        </article>
    );
}

export default Product;
