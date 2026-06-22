import { afterEach, describe, expect, it } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import Cart from "./Cart";
import cartStore from "../store/cart-store";

describe("Cart", () => {
    afterEach(cleanup);

    it("shows the current item count and total", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 3,
                totalPrice: 249.97
            }
        });

        render(<Cart />);

        expect(screen.getByLabelText("3 items in cart")).toBeInTheDocument();
        expect(screen.getByText("3 items")).toBeInTheDocument();
        expect(screen.getByText("$249.97")).toBeInTheDocument();
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
});
