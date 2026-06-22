import axios from "axios";
import { create } from "zustand";
import {
    parseCartItemUpdateQuantityRequest,
    parseCartResponse
} from "../schema/cart-schema";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api/v1";
const CART_ID_KEY = "shopping-cart-id";
let cartCreationPromise = null;

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json"
    }
});

const apiRequest = async (path, options = {}) => {
    try {
        const response = await apiClient.request({ url: path, ...options });
        return response.status === 204 ? null : response.data;
    } catch (error) {
        if (axios.isAxiosError(error) && error.response) {
            throw new Error(`Request failed with status ${error.response.status}`);
        }
        throw error;
    }
};

const emptyCart = {
    id: null,
    items: [],
    itemsTotal: 0,
    totalPrice: 0
};

const mapCart = (cartData) => ({
    id: cartData.cartId,
    items: cartData.items.map((item) => ({
        id: item.productId,
        name: item.name,
        imgURL: item.imgUrl,
        price: Number(item.unitPrice),
        quantity: item.quantity,
        totalPrice: Number(item.lineTotal)
    })),
    itemsTotal: cartData.itemsTotal,
    totalPrice: Number(cartData.totalPrice)
});

const cartStore = create((set, get) => ({
    cart: emptyCart,
    notification: null,
    cartLoading: true,
    pendingItems: {},
    deletingCart: false,

    showNotification: (notification) => {
        set({
            notification: notification?.open === false
                ? null
                : { message: notification.message, type: notification.type }
        });
    },

    replaceCartData: (cartData) => {
        const parsedCart = parseCartResponse(cartData);
        localStorage.setItem(CART_ID_KEY, parsedCart.cartId);
        set({ cart: mapCart(parsedCart) });
    },

    setItemPending: (productId, pending) => {
        set((state) => ({
            pendingItems: {
                ...state.pendingItems,
                [productId]: pending
            }
        }));
    },

    createCart: async () => {
        if (cartCreationPromise) {
            return cartCreationPromise;
        }

        set({ cartLoading: true });
        cartCreationPromise = (async () => {
            try {
                const cartData = await apiRequest("/carts", {
                    method: "POST",
                    data: {}
                });
                get().replaceCartData(cartData);
                return cartData.cartId;
            } catch (error) {
                get().showNotification({
                    message: "Could not create a cart. Please try again.",
                    type: "error"
                });
                return null;
            } finally {
                cartCreationPromise = null;
                set({ cartLoading: false });
            }
        })();

        return cartCreationPromise;
    },

    ensureCart: async () => {
        if (get().cart.id) {
            return get().cart.id;
        }
        return get().createCart();
    },

    fetchCartData: async () => {
        set({ cartLoading: true });
        const cartId = localStorage.getItem(CART_ID_KEY);

        if (!cartId) {
            await get().createCart();
            return;
        }

        try {
            const cartData = await apiRequest(`/carts/${cartId}`);
            get().replaceCartData(cartData);
        } catch (error) {
            localStorage.removeItem(CART_ID_KEY);
            await get().createCart();
        } finally {
            set({ cartLoading: false });
        }
    },

    updateItemQuantity: async (productId, quantity) => {
        if (quantity < 1) return;

        get().setItemPending(productId, true);
        try {
            const cartId = get().cart.id;
            const cartData = await apiRequest(`/carts/${cartId}/items/${productId}`, {
                method: "PATCH",
                data: parseCartItemUpdateQuantityRequest({ quantity })
            });
            get().replaceCartData(cartData);
        } catch (error) {
            get().showNotification({
                message: "Could not update the item quantity.",
                type: "error"
            });
        } finally {
            get().setItemPending(productId, false);
        }
    },

    removeCartItem: async (productId) => {
        get().setItemPending(productId, true);
        try {
            const cartData = await apiRequest(
                `/carts/${get().cart.id}/items/${productId}`,
                { method: "DELETE" }
            );
            get().replaceCartData(cartData);
        } catch (error) {
            get().showNotification({
                message: "Could not remove the item from your cart.",
                type: "error"
            });
        } finally {
            get().setItemPending(productId, false);
        }
    },

    deleteCart: async () => {
        const cartId = get().cart.id;
        if (!cartId) return;

        set({ deletingCart: true });
        try {
            await apiRequest(`/carts/${cartId}`, { method: "DELETE" });
            localStorage.removeItem(CART_ID_KEY);
            set({ cart: emptyCart });
            get().showNotification({
                message: "Cart deleted.",
                type: "success"
            });
        } catch (error) {
            get().showNotification({
                message: "Could not delete the cart.",
                type: "error"
            });
        } finally {
            set({ deletingCart: false });
        }
    }
}));

export default cartStore;
