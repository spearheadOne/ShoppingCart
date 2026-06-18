import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import CartItemList from "./CartItemList";
import cartStore from "../store/cart-store";

describe("CartItemList", () => {
    const originalActions = {
        createCart: cartStore.getState().createCart,
        deleteCart: cartStore.getState().deleteCart,
        fetchCartData: cartStore.getState().fetchCartData
    };
    let createCart;
    let deleteCart;
    let fetchCartData;

    beforeEach(() => {
        createCart = vi.fn();
        deleteCart = vi.fn().mockResolvedValue(undefined);
        fetchCartData = vi.fn();
        cartStore.setState({
            cart: {
                id: null,
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            },
            cartLoading: false,
            deletingCart: false,
            pendingItems: {},
            createCart,
            deleteCart,
            fetchCartData
        });
    });

    afterEach(() => {
        cleanup();
        cartStore.setState(originalActions);
    });

    it("creates a cart from the no-active-cart state", async () => {
        const user = userEvent.setup();
        render(<CartItemList />);

        expect(screen.getByText("No active cart")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "Create cart" }));
        expect(createCart).toHaveBeenCalledOnce();
    });

    it("renders items and totals and refreshes the cart", async () => {
        const user = userEvent.setup();
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [{
                    id: "product-1",
                    name: "Keyboard",
                    imgURL: "/keyboard.png",
                    price: 99.99,
                    quantity: 2,
                    totalPrice: 199.98
                }],
                itemsTotal: 2,
                totalPrice: 199.98
            }
        });

        render(<CartItemList />);

        expect(screen.getByText("ID: cart-1")).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: "Keyboard" })).toBeInTheDocument();
        expect(screen.getAllByText("$199.98")).toHaveLength(2);

        await user.click(screen.getByRole("button", { name: "Refresh" }));
        expect(fetchCartData).toHaveBeenCalledOnce();
    });

    it("requires confirmation before deleting the cart", async () => {
        const user = userEvent.setup();
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            }
        });

        render(<CartItemList />);

        await user.click(screen.getByRole("button", { name: "Delete cart" }));
        expect(deleteCart).not.toHaveBeenCalled();
        expect(screen.getByText("Delete this cart?")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "Yes, delete" }));
        expect(deleteCart).toHaveBeenCalledOnce();
    });
});
