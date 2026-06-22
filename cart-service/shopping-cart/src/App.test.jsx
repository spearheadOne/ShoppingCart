import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import App from "./App";
import cartStore from "./store/cart-store";

describe("App", () => {
    const originalActions = {
        fetchCartData: cartStore.getState().fetchCartData,
        synchronizeCart: cartStore.getState().synchronizeCart
    };

    afterEach(() => {
        cleanup();
        cartStore.setState({
            ...originalActions,
            notification: null
        });
    });

    it("loads cart data on mount", async () => {
        const fetchCartData = vi.fn();
        cartStore.setState({
            fetchCartData,
            notification: null,
            cart: {
                id: null,
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            },
            cartLoading: false,
        });

        render(<App />);

        await waitFor(() => {
            expect(fetchCartData).toHaveBeenCalledOnce();
        });
        expect(screen.getByText("Shopping cart")).toBeInTheDocument();
    });

    it("renders the current notification", () => {
        cartStore.setState({
            fetchCartData: vi.fn(),
            notification: {
                type: "error",
                message: "Cart failed to load."
            },
            cartLoading: false,
        });

        render(<App />);

        expect(screen.getByText("Cart failed to load.")).toBeInTheDocument();
    });

    it("refreshes the cart when the combined UI synchronizes a cart id", async () => {
        const synchronizeCart = vi.fn();
        cartStore.setState({
            fetchCartData: vi.fn(),
            synchronizeCart,
            notification: null,
            cartLoading: false
        });

        render(<App />);
        window.dispatchEvent(new MessageEvent("message", {
            data: {
                type: "shopping-cart:synchronize",
                cartId: "cart-2"
            }
        }));

        await waitFor(() => {
            expect(synchronizeCart).toHaveBeenCalledWith("cart-2");
        });
    });
});
