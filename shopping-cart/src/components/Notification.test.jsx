import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import Notification from "./Notification";
import cartStore from "../store/cart-store";

describe("Notification", () => {
    const originalShowNotification = cartStore.getState().showNotification;

    afterEach(() => {
        cleanup();
        cartStore.setState({ showNotification: originalShowNotification });
    });

    it("renders a message and closes it through the store", async () => {
        const user = userEvent.setup();
        const showNotification = vi.fn();
        cartStore.setState({ showNotification });

        render(<Notification type="error" message="Could not load the cart." />);

        expect(screen.getByText("Could not load the cart.")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "Close" }));
        expect(showNotification).toHaveBeenCalledWith({ open: false });
    });
});
