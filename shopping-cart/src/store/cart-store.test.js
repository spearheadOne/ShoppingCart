import { beforeEach, describe, expect, it, vi } from "vitest";

const { requestMock, createMock } = vi.hoisted(() => {
    const request = vi.fn();
    return {
        requestMock: request,
        createMock: vi.fn(() => ({ request }))
    };
});

vi.mock("axios", () => ({
    default: {
        create: createMock,
        isAxiosError: vi.fn(() => false)
    }
}));

import cartStore from "./cart-store";

const emptyCartResponse = {
    cartId: "cart-1",
    items: [],
    itemsTotal: 0,
    totalPrice: 0
};

const cartWithKeyboard = (quantity = 2) => ({
    cartId: "cart-1",
    items: [{
        productId: "product-1",
        name: "Keyboard",
        imgUrl: "/keyboard.png",
        unitPrice: 99.99,
        quantity,
        lineTotal: 99.99 * quantity
    }],
    itemsTotal: quantity,
    totalPrice: 99.99 * quantity
});

const apiResponse = (data, status = 200) => ({ data, status });

const resetStore = () => {
    cartStore.setState({
        cart: {
            id: null,
            items: [],
            itemsTotal: 0,
            totalPrice: 0
        },
        products: [],
        notification: null,
        cartLoading: false,
        productsLoading: false,
        pendingItems: {},
        deletingCart: false
    });
};

describe("cart store API integration", () => {
    beforeEach(() => {
        requestMock.mockReset();
        localStorage.clear();
        resetStore();
    });

    it("configures the API client for JSON requests", () => {
        expect(createMock).toHaveBeenCalledWith({
            baseURL: "/api/v1",
            headers: {
                "Content-Type": "application/json"
            }
        });
    });

    it("creates a cart with an explicit JSON body", async () => {
        requestMock.mockResolvedValueOnce(apiResponse(emptyCartResponse, 201));

        const cartId = await cartStore.getState().createCart();

        expect(requestMock).toHaveBeenCalledWith({
            url: "/carts",
            method: "POST",
            data: {}
        });
        expect(cartId).toBe("cart-1");
        expect(cartStore.getState().cart.id).toBe("cart-1");
        expect(localStorage.getItem("shopping-cart-id")).toBe("cart-1");
    });

    it("reads a saved cart", async () => {
        localStorage.setItem("shopping-cart-id", "cart-1");
        requestMock.mockResolvedValueOnce(apiResponse(cartWithKeyboard()));

        await cartStore.getState().fetchCartData();

        expect(requestMock).toHaveBeenCalledWith({ url: "/carts/cart-1" });
        expect(cartStore.getState().cart).toMatchObject({
            id: "cart-1",
            itemsTotal: 2,
            totalPrice: 199.98
        });
        expect(cartStore.getState().cart.items[0]).toMatchObject({
            id: "product-1",
            name: "Keyboard",
            quantity: 2,
            price: 99.99
        });
    });

    it("adds a new product to the cart", async () => {
        cartStore.setState({ cart: { ...cartStore.getState().cart, id: "cart-1" } });
        requestMock.mockResolvedValueOnce(apiResponse(cartWithKeyboard(1)));

        await cartStore.getState().addToCart({
            id: "product-1",
            name: "Keyboard",
            price: 99.99
        });

        expect(requestMock).toHaveBeenCalledWith({
            url: "/carts/cart-1/items",
            method: "POST",
            data: {
                productId: "product-1",
                quantity: 1
            }
        });
        expect(cartStore.getState().cart.items[0].quantity).toBe(1);
    });

    it("increases an existing product using the quantity endpoint", async () => {
        cartStore.getState().replaceCartData(cartWithKeyboard(2));
        requestMock.mockResolvedValueOnce(apiResponse(cartWithKeyboard(3)));

        await cartStore.getState().addToCart({
            id: "product-1",
            name: "Keyboard",
            price: 99.99
        });

        expect(requestMock).toHaveBeenCalledWith({
            url: "/carts/cart-1/items/product-1",
            method: "PATCH",
            data: { quantity: 3 }
        });
        expect(cartStore.getState().cart.items[0].quantity).toBe(3);
    });

    it("sets an exact item quantity", async () => {
        cartStore.getState().replaceCartData(cartWithKeyboard(2));
        requestMock.mockResolvedValueOnce(apiResponse(cartWithKeyboard(5)));

        await cartStore.getState().updateItemQuantity("product-1", 5);

        expect(requestMock).toHaveBeenCalledWith({
            url: "/carts/cart-1/items/product-1",
            method: "PATCH",
            data: { quantity: 5 }
        });
        expect(cartStore.getState().cart.items[0].quantity).toBe(5);
    });

    it("removes an item from the cart", async () => {
        cartStore.getState().replaceCartData(cartWithKeyboard());
        requestMock.mockResolvedValueOnce(apiResponse(emptyCartResponse));

        await cartStore.getState().removeCartItem("product-1");

        expect(requestMock).toHaveBeenCalledWith({
            url: "/carts/cart-1/items/product-1",
            method: "DELETE"
        });
        expect(cartStore.getState().cart.items).toEqual([]);
    });

    it("deletes the active cart and clears its persisted id", async () => {
        cartStore.getState().replaceCartData(cartWithKeyboard());
        requestMock.mockResolvedValueOnce(apiResponse(null, 204));

        await cartStore.getState().deleteCart();

        expect(requestMock).toHaveBeenCalledWith({
            url: "/carts/cart-1",
            method: "DELETE"
        });
        expect(cartStore.getState().cart).toEqual({
            id: null,
            items: [],
            itemsTotal: 0,
            totalPrice: 0
        });
        expect(localStorage.getItem("shopping-cart-id")).toBeNull();
        expect(cartStore.getState().notification).toEqual({
            message: "Cart deleted.",
            type: "success"
        });
    });
});
