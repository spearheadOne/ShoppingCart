import { afterEach, describe, expect, it } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import Header from "./Header";
import cartStore from "../store/cart-store";

describe("Header", () => {
    afterEach(cleanup);

    it("renders simplified branding and the cart summary", () => {
        cartStore.setState({
            cart: {
                id: "cart-1",
                items: [],
                itemsTotal: 2,
                totalPrice: 149.98
            }
        });

        render(<Header />);

        expect(screen.getByText("Shopping cart")).toBeInTheDocument();
        expect(screen.getByLabelText("2 items in cart")).toBeInTheDocument();
    });
});
