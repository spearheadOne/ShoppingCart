import axios from "axios";
import { create } from "zustand";
import {
    parseCartItemAddRequest,
    parseCartItemUpdateQuantityRequest,
    parseCartResponse
} from "../schema/cart-schema";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api/v1";
const CART_ID_KEY = "shopping-cart-id";

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json"
    }
});

const apiRequest = async (path, options = {}) => {
    const { headers, ...requestOptions } = options;

    try {
        const resp = await apiClient.request({
            url: path,
            ...requestOptions,
            headers: {
                "Content-Type": "application/json",
                ...(headers || {})
            }
        });

        if (resp.status === 204) {
            return null;
        }

        return resp.data;
    } catch (err) {
        if (axios.isAxiosError(err) && err.response) {
            throw new Error(`Request failed with status ${err.response.status}`);
        }

        throw err;
    }
};

const initialCart = {
    id: null,
    items: [],
    itemsTotal: 0,
    showCart: false,
    changed: false
};

const mapProducts = (productData = {}) => (
    (productData.products || productData || []).map((product) => ({
        id: product.id,
        name: product.name,
        imgURL: product.imgUrl,
        price: Number(product.price)
    }))
);

const mapCart = (cartData) => ({
    id: cartData.cartId,
    items: (cartData.items || []).map((item) => ({
        id: item.productId,
        name: item.name,
        imgURL: item.imgUrl,
        price: Number(item.unitPrice),
        quantity: item.quantity,
        totalPrice: Number(item.lineTotal)
    })),
    itemsTotal: cartData.itemsTotal || 0,
    showCart: false,
    changed: false
});

const cartStore = create((set, get) => ({
    cart: initialCart,
    products: [],
    notification: null,

    showNotification: (notification) => {
        set({
            notification: {
                message: notification.message,
                type: notification.type,
                open: notification.open
            }
        });
    },

    syncCartItem: async (productId, quantity, itemExists) => {
        const cartId = get().cart.id;

        if (!cartId) {
            return;
        }

        const path = `/carts/${cartId}/items${itemExists ? `/${productId}` : ""}`;
        const method = itemExists ? "PATCH" : "POST";
        const body = itemExists
            ? parseCartItemUpdateQuantityRequest({ quantity })
            : parseCartItemAddRequest({ productId, quantity });

        const cartData = await apiRequest(path, {
            method,
            data: body
        });

        get().replaceCartData(parseCartResponse(cartData));
    },

    addToCart: async (newItem) => {
        const currentItem = get().cart.items.find((item) => item.id === newItem.id);
        const nextQuantity = currentItem ? currentItem.quantity + 1 : 1;

        set((state) => {
            const existingItem = state.cart.items.find((item) => item.id === newItem.id);

            if (existingItem) {
                return {
                    cart: {
                        ...state.cart,
                        changed: true,
                        items: state.cart.items.map((item) =>
                            item.id === newItem.id
                                ? {
                                    ...item,
                                    quantity: item.quantity + 1,
                                    totalPrice: item.totalPrice + newItem.price
                                }
                                : item
                        )
                    }
                };
            }

            return {
                cart: {
                    ...state.cart,
                    changed: true,
                    itemsTotal: state.cart.itemsTotal + 1,
                    items: [
                        ...state.cart.items,
                        {
                            id: newItem.id,
                            price: newItem.price,
                            quantity: 1,
                            totalPrice: newItem.price,
                            name: newItem.name
                        }
                    ]
                }
            };
        });

        try {
            await get().syncCartItem(newItem.id, nextQuantity, Boolean(currentItem));
        } catch (err) {
            get().showNotification({
                open: true,
                message: "Error updating cart",
                type: "error"
            });
        }
    },

    removeFromCart: async (id) => {
        const currentItem = get().cart.items.find((item) => item.id === id);
        const nextQuantity = currentItem ? currentItem.quantity - 1 : 0;

        set((state) => {
            const existingItem = state.cart.items.find((item) => item.id === id);

            if (!existingItem) {
                return state;
            }

            if (existingItem.quantity === 1) {
                return {
                    cart: {
                        ...state.cart,
                        changed: true,
                        itemsTotal: state.cart.itemsTotal - 1,
                        items: state.cart.items.filter((item) => item.id !== id)
                    }
                };
            }

            return {
                cart: {
                    ...state.cart,
                    changed: true,
                    items: state.cart.items.map((item) =>
                        item.id === id
                            ? {
                                ...item,
                                quantity: item.quantity - 1,
                                totalPrice: item.totalPrice - item.price
                            }
                            : item
                    )
                }
            };
        });

        try {
            const cartId = get().cart.id;

            if (!cartId || !currentItem) {
                return;
            }

            const cartData = nextQuantity > 0
                ? await apiRequest(`/carts/${cartId}/items/${id}`, {
                    method: "PATCH",
                    data: parseCartItemUpdateQuantityRequest({
                        quantity: nextQuantity
                    })
                })
                : await apiRequest(`/carts/${cartId}/items/${id}`, {
                    method: "DELETE"
                });

            get().replaceCartData(parseCartResponse(cartData));
        } catch (err) {
            get().showNotification({
                open: true,
                message: "Error updating cart",
                type: "error"
            });
        }
    },

    toggleCart: () => {
        set((state) => ({
            cart: {
                ...state.cart,
                showCart: !state.cart.showCart
            }
        }));
    },

    replaceCartData: (cartData) => {
        if (!cartData) {
            return;
        }

        const parsedCart = parseCartResponse(cartData);

        localStorage.setItem(CART_ID_KEY, parsedCart.cartId);

        set((state) => ({
            cart: {
                ...state.cart,
                ...mapCart(parsedCart),
                showCart: state.cart.showCart
            }
        }));
    },

    ensureCart: async () => {
        const existingCartId = localStorage.getItem(CART_ID_KEY);

        if (existingCartId) {
            return existingCartId;
        }

        const cartData = parseCartResponse(await apiRequest("/carts", { method: "POST" }));
        get().replaceCartData(cartData);

        return cartData.cartId;
    },

    fetchCartData: async () => {
        try {
            const cartId = await get().ensureCart();
            const cartData = parseCartResponse(await apiRequest(`/carts/${cartId}`));
            get().replaceCartData(cartData);
        } catch (err) {
            get().showNotification({
                open: true,
                message: "Error reading cart data",
                type: "error"
            });
        }
    },

    sendCartData: async (cart) => {
        if (!cart.changed) {
            return;
        }

        set((state) => ({
            cart: {
                ...state.cart,
                changed: false
            }
        }));
    },

    fetchProducts: async () => {
        try {
            const productData = await apiRequest("/products");
            set({ products: mapProducts(productData) });
        } catch (err) {
            get().showNotification({
                open: true,
                message: "Error reading product data",
                type: "error"
            });
        }
    }
}));

export default cartStore;
