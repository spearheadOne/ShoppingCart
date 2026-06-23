import axios from "axios";
import { create } from "zustand";
import {
    parseCartItemUpdateQuantityRequest,
    parseCartResponse
} from "../schema/cart-schema";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api/v1";
const CART_ID_KEY = "shopping-cart-id";
let cartGeneration = 0;

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
    submittingCart: false,

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

    fetchCartById: async (cartId) => {
        const generation = ++cartGeneration;
        set({ cartLoading: true });
        try {
            const cartData = await apiRequest(`/carts/${cartId}`);
            if (generation !== cartGeneration) {
                return null;
            }
            get().replaceCartData(cartData);
            return true;
        } catch (error) {
            if (generation !== cartGeneration) {
                return null;
            }
            localStorage.removeItem(CART_ID_KEY);
            return false;
        } finally {
            set({ cartLoading: false });
        }
    },

    synchronizeCart: async (cartId) => {
        if (!cartId) return false;

        localStorage.setItem(CART_ID_KEY, cartId);
        return get().fetchCartById(cartId);
    },

    fetchCartData: async () => {
        const cartId = localStorage.getItem(CART_ID_KEY);

        if (!cartId) {
            set({ cart: emptyCart, cartLoading: false });
            return;
        }

        const found = await get().fetchCartById(cartId);
        if (found === false) {
            set({ cart: emptyCart, cartLoading: false });
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
            window.parent.postMessage({
                type: "shopping-cart:changed",
                cartId
            }, "*");
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
            window.parent.postMessage({
                type: "shopping-cart:changed",
                cartId: cartData.cartId
            }, "*");
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
            window.parent.postMessage({ type: "shopping-cart:cleared" }, "*");
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
    },

    submitCart: async () => {
        const cartId = get().cart.id;
        if (!cartId || get().cart.items.length === 0) return;

        set({ submittingCart: true });
        try {
            const orderData = await apiRequest(`/carts/${cartId}/submit`, {
                method: "POST"
            });
            localStorage.removeItem(CART_ID_KEY);
            set({ cart: emptyCart });
            window.parent.postMessage({ type: "shopping-cart:cleared" }, "*");
            get().showNotification({
                message: orderData?.orderId
                    ? `Order ${orderData.orderId} created.`
                    : "Order created.",
                type: "success"
            });
        } catch (error) {
            get().showNotification({
                message: "Could not submit the cart.",
                type: "error"
            });
        } finally {
            set({ submittingCart: false });
        }
    }
}));

export default cartStore;
