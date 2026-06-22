import { useEffect } from "react";
import Layout from "./components/Layout";
import Notification from "./components/Notification";
import cartStore from "./store/cart-store";

function App() {
    const notification = cartStore((state) => state.notification);
    const fetchCartData = cartStore((state) => state.fetchCartData);
    const synchronizeCart = cartStore((state) => state.synchronizeCart);
    const cart = cartStore((state) => state.cart);

    useEffect(() => {
        if (window.parent === window) {
            fetchCartData();
            return;
        }

        window.parent.postMessage({ type: "shopping-cart:request-state" }, "*");
        const initialLoad = window.setTimeout(() => {
            if (!cartStore.getState().cart.id) {
                fetchCartData();
            }
        }, 500);

        return () => window.clearTimeout(initialLoad);
    }, [fetchCartData]);

    useEffect(() => {
        const refreshCart = (event) => {
            if (
                ["shopping-cart:changed", "shopping-cart:synchronize"]
                    .includes(event.data?.type)
                && event.data.cartId
            ) {
                synchronizeCart(event.data.cartId);
            }
            if (event.data?.type === "shopping-cart:request-state") {
                const currentCartId = cartStore.getState().cart.id;
                if (!currentCartId) return;
                window.parent.postMessage({
                    type: "shopping-cart:state",
                    cartId: currentCartId
                }, "*");
            }
        };
        window.addEventListener("message", refreshCart);
        return () => window.removeEventListener("message", refreshCart);
    }, [synchronizeCart]);

    useEffect(() => {
        if (cart.id) {
            window.parent.postMessage({
                type: "shopping-cart:state",
                cartId: cart.id
            }, "*");
        }
    }, [cart]);

    return (
        <>
            {notification && (
                <Notification
                    type={notification.type}
                    message={notification.message}
                />
            )}
            <Layout />
        </>
    );
}

export default App;
