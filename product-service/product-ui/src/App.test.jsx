import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, waitFor } from "@testing-library/react";
import App from "./App";
import productStore from "./store/product-store";

describe("App cart synchronization", () => {
    const originalActions = {
        fetchProducts: productStore.getState().fetchProducts,
        fetchCart: productStore.getState().fetchCart,
        clearCart: productStore.getState().clearCart
    };

    afterEach(() => {
        cleanup();
        productStore.setState({
            ...originalActions,
            notification: null
        });
    });

    it("reloads cart quantities when the combined UI provides a cart id", async () => {
        const fetchProducts = vi.fn();
        const fetchCart = vi.fn();
        productStore.setState({
            fetchProducts,
            fetchCart,
            products: [],
            productsLoading: false,
            notification: null
        });

        render(<App />);
        window.dispatchEvent(new MessageEvent("message", {
            data: {
                type: "shopping-cart:synchronize",
                cartId: "cart-2"
            }
        }));

        await waitFor(() => {
            expect(fetchCart).toHaveBeenCalledWith("cart-2");
        });
    });

    it("clears product quantities after cart deletion", async () => {
        const clearCart = vi.fn();
        productStore.setState({
            fetchProducts: vi.fn(),
            fetchCart: vi.fn(),
            clearCart,
            products: [],
            productsLoading: false,
            notification: null
        });

        render(<App />);
        window.dispatchEvent(new MessageEvent("message", {
            data: { type: "shopping-cart:cleared" }
        }));

        await waitFor(() => {
            expect(clearCart).toHaveBeenCalledOnce();
        });
    });
});
