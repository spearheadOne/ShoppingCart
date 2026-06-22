import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import CartItem from "./CartItem";
import cartStore from "../store/cart-store";

const item = {
    id: "product-1",
    name: "Keyboard",
    imgURL: "/keyboard.png",
    price: 99.99,
    quantity: 2,
    totalPrice: 199.98
};

describe("CartItem", () => {
    const originalUpdateItemQuantity = cartStore.getState().updateItemQuantity;
    const originalRemoveCartItem = cartStore.getState().removeCartItem;
    let updateItemQuantity;
    let removeCartItem;

    beforeEach(() => {
        updateItemQuantity = vi.fn();
        removeCartItem = vi.fn();
        cartStore.setState({
            pendingItems: {},
            updateItemQuantity,
            removeCartItem
        });
    });

    afterEach(() => {
        cleanup();
        cartStore.setState({
            pendingItems: {},
            updateItemQuantity: originalUpdateItemQuantity,
            removeCartItem: originalRemoveCartItem
        });
    });

    it("shows cart item pricing and quantity", () => {
        render(<CartItem {...item} />);

        expect(screen.getByRole("heading", { name: "Keyboard" })).toBeInTheDocument();
        expect(screen.getByText("$99.99 each")).toBeInTheDocument();
        expect(screen.getByLabelText("Quantity")).toHaveValue(2);
        expect(screen.getByText("$199.98")).toBeInTheDocument();
    });

    it("increments, decrements, and removes an item", async () => {
        const user = userEvent.setup();
        render(<CartItem {...item} />);

        await user.click(screen.getByRole("button", { name: "Increase Keyboard quantity" }));
        await user.click(screen.getByRole("button", { name: "Decrease Keyboard quantity" }));
        await user.click(screen.getByRole("button", { name: "Remove" }));

        expect(updateItemQuantity).toHaveBeenNthCalledWith(1, "product-1", 3);
        expect(updateItemQuantity).toHaveBeenNthCalledWith(2, "product-1", 1);
        expect(removeCartItem).toHaveBeenCalledWith("product-1");
    });

    it("submits an exact quantity", async () => {
        const user = userEvent.setup();
        render(<CartItem {...item} />);

        const quantityInput = screen.getByLabelText("Quantity");
        await user.clear(quantityInput);
        await user.type(quantityInput, "5");
        await user.click(screen.getByRole("button", { name: "Update" }));

        expect(updateItemQuantity).toHaveBeenCalledWith("product-1", 5);
    });
});
