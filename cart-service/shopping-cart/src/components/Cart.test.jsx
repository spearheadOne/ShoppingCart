import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Cart from "./Cart";
import cartStore from "../store/cart-store";

describe("Cart", () => {
    const originalSubmitCart = cartStore.getState().submitCart;
    let submitCart;

    beforeEach(() => {
        submitCart = vi.fn();
        cartStore.setState({
            cartLoading: false,
            deletingCart: false,
            submittingCart: false,
            submitCart
        });
    });

    afterEach(() => {
        cleanup();
        cartStore.setState({
            submitCart: originalSubmitCart,
            submittingCart: false
        });
    });

    it("shows the current item count and total", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [{
                    id: "product-1",
                    name: "Keyboard",
                    imgURL: "/keyboard.png",
                    price: 99.99,
                    quantity: 3,
                    totalPrice: 249.97
                }],
                itemsTotal: 3,
                totalPrice: 249.97
            }
        });

        render(<Cart />);

        expect(screen.getByLabelText("3 items in cart")).toBeInTheDocument();
        expect(screen.getByText("3 items")).toBeInTheDocument();
        expect(screen.getByText("$249.97")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Submit" })).toBeEnabled();
    });

    it("uses the singular item label", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 1,
                totalPrice: 99.99
            }
        });

        render(<Cart />);

        expect(screen.getByText("1 item")).toBeInTheDocument();
    });

    it("submits the current cart", async () => {
        const user = userEvent.setup();
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [{
                    id: "product-1",
                    name: "Keyboard",
                    imgURL: "/keyboard.png",
                    price: 99.99,
                    quantity: 1,
                    totalPrice: 99.99
                }],
                itemsTotal: 1,
                totalPrice: 99.99
            }
        });

        render(<Cart />);

        await user.click(screen.getByRole("button", { name: "Submit" }));

        expect(submitCart).toHaveBeenCalledOnce();
    });

    it("disables submit when the cart has no items", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            }
        });

        render(<Cart />);

        expect(screen.getByRole("button", { name: "Submit" })).toBeDisabled();
    });
});
