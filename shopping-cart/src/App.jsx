import { useEffect } from "react";
import Layout from "./components/Layout";
import Notification from "./components/Notification";
import cartStore from "./store/cart-store";

function App() {
    const notification = cartStore((state) => state.notification);
    const fetchCartData = cartStore((state) => state.fetchCartData);
    const fetchProducts = cartStore((state) => state.fetchProducts);

    useEffect(() => {
        fetchCartData();
        fetchProducts();
    }, [fetchCartData, fetchProducts]);

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
