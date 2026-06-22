import { afterEach, describe, expect, it } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import Layout from "./Layout";
import cartStore from "../store/cart-store";

describe("Layout", () => {
    afterEach(cleanup);

    it("renders cart management without product catalog components", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            },
            cartLoading: false,
            pendingItems: {},
            deletingCart: false
        });

        render(<Layout />);

        const cartHeading = screen.getByRole("heading", { name: "Review your cart" });
        expect(cartHeading).toBeInTheDocument();
        expect(screen.queryByRole("heading", { name: "Add products" })).not.toBeInTheDocument();
        expect(screen.getByText("Your cart is empty")).toBeInTheDocument();
    });
});
