
import { create } from "zustand";

const CART_URL = "https://shoppingcart-a62bb-default-rtdb.europe-west1.firebasedatabase.app/cartItems.json";
const PRODUCTS_URL = "https://shoppingcart-a62bb-default-rtdb.europe-west1.firebasedatabase.app/products.json";

const initialCart = {
    items: [],
    itemsTotal: 0,
    showCart: false,
    changed: false
};

const mapProducts = (rawProducts = {}) => {
    return Object.entries(rawProducts).map(([id, product]) => ({
        id,
        name: product.name,
        imgURL: product.imgUrl,
        price: product.price
    }));
};

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

    addToCart: (newItem) => {
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
    },

    removeFromCart: (id) => {
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

        set((state) => ({
            cart: {
                ...state.cart,
                items: cartData.items || [],
                itemsTotal: cartData.itemsTotal ?? cartData.totalPrice ?? 0,
                changed: false
            }
        }));
    },

    fetchCartData: async () => {
        try {
            const resp = await fetch(CART_URL);
            const cartData = await resp.json();
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
        get().showNotification({
            open: true,
            message: "Sending request",
            type: "warning"
        });

        try {
            await fetch(CART_URL, {
                method: "PUT",
                body: JSON.stringify(cart)
            });

            get().showNotification({
                open: true,
                message: "Data sent succesfully",
                type: "success"
            });
        } catch (err) {
            get().showNotification({
                open: true,
                message: "Error sending data",
                type: "error"
            });
        }
    },

    fetchProducts: async () => {
        try {
            const resp = await fetch(PRODUCTS_URL);
            const productData = await resp.json();
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
