import { useEffect, useState } from "react";
import cartStore from "../store/cart-store";
import "../stylesheets/CartItem.css";

function CartItem({ id, name, imgURL, price, quantity, totalPrice }) {
    const [nextQuantity, setNextQuantity] = useState(quantity);
    const updateItemQuantity = cartStore((state) => state.updateItemQuantity);
    const removeCartItem = cartStore((state) => state.removeCartItem);
    const pending = cartStore((state) => Boolean(state.pendingItems[id]));

    useEffect(() => {
        setNextQuantity(quantity);
    }, [quantity]);

    const submitQuantity = (event) => {
        event.preventDefault();
        const parsedQuantity = Number(nextQuantity);
        if (Number.isInteger(parsedQuantity) && parsedQuantity > 0 && parsedQuantity !== quantity) {
            updateItemQuantity(id, parsedQuantity);
        }
    };

    return (
        <article className="cart-item">
            <img src={imgURL} alt="" />
            <div className="cart-item-details">
                <h3>{name}</h3>
                <span>${price.toFixed(2)} each</span>
                <button
                    className="remove-item-button"
                    onClick={() => removeCartItem(id)}
                    disabled={pending}
                >
                    Remove
                </button>
            </div>

            <form className="quantity-form" onSubmit={submitQuantity}>
                <label htmlFor={`quantity-${id}`}>Quantity</label>
                <div className="quantity-control">
                    <button
                        type="button"
                        onClick={() => updateItemQuantity(id, quantity - 1)}
                        disabled={pending || quantity === 1}
                        aria-label={`Decrease ${name} quantity`}
                    >
                        −
                    </button>
                    <input
                        id={`quantity-${id}`}
                        type="number"
                        min="1"
                        step="1"
                        value={nextQuantity}
                        onChange={(event) => setNextQuantity(event.target.value)}
                        disabled={pending}
                    />
                    <button
                        type="button"
                        onClick={() => updateItemQuantity(id, quantity + 1)}
                        disabled={pending}
                        aria-label={`Increase ${name} quantity`}
                    >
                        +
                    </button>
                </div>
                {Number(nextQuantity) !== quantity && (
                    <button className="update-quantity-button" disabled={pending}>
                        Update
                    </button>
                )}
            </form>

            <strong className="line-total">${totalPrice.toFixed(2)}</strong>
        </article>
    );
}

export default CartItem;
