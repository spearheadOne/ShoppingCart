import { useEffect } from "react";
import Layout from "./components/Layout";
import Notification from "./components/Notification";
import cartStore from "./store/cart-store";

function App() {
    const notification = cartStore((state) => state.notification);
    const fetchCartData = cartStore((state) => state.fetchCartData);
    const cartId = cartStore((state) => state.cart.id);

    useEffect(() => {
        fetchCartData();
    }, [fetchCartData]);

    useEffect(() => {
        const refreshCart = (event) => {
            if (event.data?.type === "shopping-cart:changed") {
                if (event.data.cartId) {
                    localStorage.setItem("shopping-cart-id", event.data.cartId);
                }
                fetchCartData();
            }
            if (event.data?.type === "shopping-cart:request-state" && cartId) {
                window.parent.postMessage({
                    type: "shopping-cart:state",
                    cartId
                }, "*");
            }
        };
        window.addEventListener("message", refreshCart);
        return () => window.removeEventListener("message", refreshCart);
    }, [cartId, fetchCartData]);

    useEffect(() => {
        if (cartId) {
            window.parent.postMessage({
                type: "shopping-cart:state",
                cartId
            }, "*");
        }
    }, [cartId]);

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
