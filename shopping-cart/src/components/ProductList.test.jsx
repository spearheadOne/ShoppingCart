import { afterEach, describe, expect, it } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import ProductList from "./ProductList";
import cartStore from "../store/cart-store";

describe("ProductList", () => {
    afterEach(cleanup);

    it("shows a loading state", () => {
        cartStore.setState({ products: [], productsLoading: true });
        render(<ProductList />);
        expect(screen.getByText("Loading products…")).toBeInTheDocument();
    });

    it("shows an empty state", () => {
        cartStore.setState({ products: [], productsLoading: false });
        render(<ProductList />);
        expect(screen.getByText("No products are available.")).toBeInTheDocument();
    });

    it("renders all products", () => {
        cartStore.setState({
            productsLoading: false,
            products: [
                { id: "product-1", name: "Keyboard", imgURL: "/keyboard.png", price: 99.99 },
                { id: "product-2", name: "Mouse", imgURL: "/mouse.png", price: 49.99 }
            ],
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 0,
                totalPrice: 0
            },
            pendingItems: {}
        });

        render(<ProductList />);

        expect(screen.getByRole("heading", { name: "Keyboard" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: "Mouse" })).toBeInTheDocument();
    });
});
