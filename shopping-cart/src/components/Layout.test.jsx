import { afterEach, describe, expect, it } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import Layout from "./Layout";
import cartStore from "../store/cart-store";

describe("Layout", () => {
    afterEach(cleanup);

    it("places cart management before the separated product catalog", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            },
            cartLoading: false,
            products: [],
            productsLoading: false,
            pendingItems: {},
            deletingCart: false
        });

        render(<Layout />);

        const cartHeading = screen.getByRole("heading", { name: "Review your cart" });
        const catalogHeading = screen.getByRole("heading", { name: "Add products" });

        expect(cartHeading).toBeInTheDocument();
        expect(catalogHeading).toBeInTheDocument();
        expect(
            cartHeading.compareDocumentPosition(catalogHeading)
                & Node.DOCUMENT_POSITION_FOLLOWING
        ).toBeTruthy();
        expect(screen.getByText("Your cart is empty")).toBeInTheDocument();
        expect(screen.getByText("No products are available.")).toBeInTheDocument();
    });
});
