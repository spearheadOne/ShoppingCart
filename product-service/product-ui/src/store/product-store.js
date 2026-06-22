import axios from "axios";
import { create } from "zustand";

const PRODUCT_API_BASE_URL = import.meta.env.VITE_PRODUCT_API_BASE_URL || "/api/v1";
const CART_API_BASE_URL = import.meta.env.VITE_CART_API_BASE_URL
    || (import.meta.env.DEV ? "/cart-api/v1" : "http://localhost:8080/api/v1");
const CART_ID_KEY = "shopping-cart-id";

const productClient = axios.create({ baseURL: PRODUCT_API_BASE_URL });
const cartClient = axios.create({
    baseURL: CART_API_BASE_URL,
    headers: { "Content-Type": "application/json" }
});

const emptyCart = { id: null, items: [] };

const mapCart = (cart) => ({
    id: cart.cartId,
    items: cart.items.map((item) => ({
        id: item.productId,
        quantity: item.quantity
    }))
});

const productStore = create((set, get) => ({
    products: [],
    productsLoading: true,
    cart: emptyCart,
    pendingItems: {},
    notification: null,

    clearNotification: () => set({ notification: null }),

    fetchProducts: async () => {
        set({ productsLoading: true });
        try {
            const { data } = await productClient.get("/products");
            set({
                products: data.products.map((product) => ({
                    ...product,
                    imgURL: product.imgUrl,
                    price: Number(product.price)
                }))
            });
        } catch {
            set({ notification: { type: "error", message: "Could not load the product catalog." } });
        } finally {
            set({ productsLoading: false });
        }
    },

    fetchCart: async () => {
        const cartId = localStorage.getItem(CART_ID_KEY);
        if (!cartId) return;

        try {
            const { data } = await cartClient.get(`/carts/${cartId}`);
            set({ cart: mapCart(data) });
        } catch {
            localStorage.removeItem(CART_ID_KEY);
            set({ cart: emptyCart });
        }
    },

    ensureCart: async () => {
        if (get().cart.id) return get().cart.id;

        const { data } = await cartClient.post("/carts", {});
        localStorage.setItem(CART_ID_KEY, data.cartId);
        set({ cart: mapCart(data) });
        return data.cartId;
    },

    addToCart: async (product) => {
        set((state) => ({
            pendingItems: { ...state.pendingItems, [product.id]: true }
        }));

        try {
            const cartId = await get().ensureCart();
            const existing = get().cart.items.find((item) => item.id === product.id);
            const response = existing
                ? await cartClient.patch(`/carts/${cartId}/items/${product.id}`, {
                    quantity: existing.quantity + 1
                })
                : await cartClient.post(`/carts/${cartId}/items`, {
                    productId: product.id,
                    quantity: 1
                });

            set({ cart: mapCart(response.data) });
            window.parent.postMessage({
                type: "shopping-cart:changed",
                cartId: get().cart.id
            }, "*");
        } catch {
            set({ notification: { type: "error", message: "Could not add the item to the cart." } });
        } finally {
            set((state) => ({
                pendingItems: { ...state.pendingItems, [product.id]: false }
            }));
        }
    }
}));

export default productStore;
