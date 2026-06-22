import { useEffect } from "react";
import ProductList from "./components/ProductList";
import productStore from "./store/product-store";
import "./stylesheets/Layout.css";

function App() {
    const fetchProducts = productStore((state) => state.fetchProducts);
    const fetchCart = productStore((state) => state.fetchCart);
    const notification = productStore((state) => state.notification);
    const clearNotification = productStore((state) => state.clearNotification);
    const clearCart = productStore((state) => state.clearCart);

    useEffect(() => {
        fetchProducts();
        fetchCart();
        window.parent.postMessage({ type: "shopping-cart:request-state" }, "*");
    }, [fetchProducts, fetchCart]);

    useEffect(() => {
        const synchronizeCart = (event) => {
            if (
                ["shopping-cart:state", "shopping-cart:synchronize"]
                    .includes(event.data?.type)
                && event.data.cartId
            ) {
                fetchCart(event.data.cartId);
            }
            if (event.data?.type === "shopping-cart:cleared") {
                clearCart();
            }
        };
        window.addEventListener("message", synchronizeCart);
        return () => window.removeEventListener("message", synchronizeCart);
    }, [clearCart, fetchCart]);

    return (
        <div className="catalog-shell">
            {notification && (
                <div className={`catalog-notification ${notification.type}`} role="status">
                    <span>{notification.message}</span>
                    <button onClick={clearNotification} aria-label="Close">×</button>
                </div>
            )}

            <main>
                <ProductList />
            </main>
        </div>
    );
}

export default App;
