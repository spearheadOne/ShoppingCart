import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Product from "./Product";
import cartStore from "../store/cart-store";

describe("Product", () => {
    const originalAddToCart = cartStore.getState().addToCart;
    let addToCart;

    beforeEach(() => {
        addToCart = vi.fn();
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            },
            pendingItems: {},
            addToCart
        });
    });

    afterEach(() => {
        cleanup();
        cartStore.setState({ addToCart: originalAddToCart });
    });

    it("renders a product and adds it to the cart", async () => {
        const user = userEvent.setup();
        render(
            <Product
                id="product-1"
                name="Keyboard"
                imgUrl="/keyboard.png"
                price={99.99}
            />
        );

        expect(screen.getByRole("heading", { name: "Keyboard" })).toBeInTheDocument();
        expect(screen.getByText("$99.99")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "Add to cart" }));

        expect(addToCart).toHaveBeenCalledWith({
            id: "product-1",
            name: "Keyboard",
            price: 99.99,
            imgURL: "/keyboard.png"
        });
    });

    it("shows the existing cart quantity and pending state", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [{ id: "product-1", quantity: 2 }],
                itemsTotal: 2,
                totalPrice: 199.98
            },
            pendingItems: { "product-1": true }
        });

        render(
            <Product
                id="product-1"
                name="Keyboard"
                imgUrl="/keyboard.png"
                price={99.99}
            />
        );

        expect(screen.getByText("2 in cart")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Adding…" })).toBeDisabled();
    });
});
