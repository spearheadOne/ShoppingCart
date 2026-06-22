const { cartUiUrl, productUiUrl } = window.shoppingCartConfig;

const cartFrame = document.getElementById("cart-ui");
const productFrame = document.getElementById("product-ui");
const cartOrigin = new URL(cartUiUrl).origin;
const productOrigin = new URL(productUiUrl).origin;
const CART_ID_KEY = "shopping-cart-id";

let cartId = localStorage.getItem(CART_ID_KEY);

const postToCart = (message) => {
    cartFrame.contentWindow?.postMessage(message, cartOrigin);
};

const postToProduct = (message) => {
    productFrame.contentWindow?.postMessage(message, productOrigin);
};

const rememberCart = (nextCartId) => {
    if (!nextCartId) return;
    cartId = nextCartId;
    localStorage.setItem(CART_ID_KEY, nextCartId);
};

const clearCart = () => {
    cartId = null;
    localStorage.removeItem(CART_ID_KEY);
};

const synchronizeFrame = (postMessage) => {
    if (cartId) {
        postMessage({ type: "shopping-cart:synchronize", cartId });
    }
};

cartFrame.addEventListener("load", () => {
    if (cartId) {
        synchronizeFrame(postToCart);
    } else {
        postToCart({ type: "shopping-cart:request-state" });
    }
});

productFrame.addEventListener("load", () => {
    synchronizeFrame(postToProduct);
});

cartFrame.src = cartUiUrl;
productFrame.src = productUiUrl;

window.addEventListener("message", (event) => {
    if (
        event.origin === productOrigin
        && event.data?.type === "shopping-cart:changed"
        && event.data.cartId
    ) {
        rememberCart(event.data.cartId);
        synchronizeFrame(postToCart);
        return;
    }

    if (
        event.origin === productOrigin
        && event.data?.type === "shopping-cart:request-state"
    ) {
        if (cartId) {
            synchronizeFrame(postToProduct);
        } else {
            postToCart(event.data);
        }
        return;
    }

    if (
        event.origin === cartOrigin
        && event.data?.type === "shopping-cart:changed"
        && event.data.cartId
    ) {
        rememberCart(event.data.cartId);
        synchronizeFrame(postToProduct);
        return;
    }

    if (
        event.origin === cartOrigin
        && event.data?.type === "shopping-cart:cleared"
    ) {
        clearCart();
        postToProduct({ type: "shopping-cart:cleared" });
        return;
    }

    if (
        event.origin === cartOrigin
        && event.data?.type === "shopping-cart:state"
        && event.data.cartId
    ) {
        rememberCart(event.data.cartId);
        synchronizeFrame(postToProduct);
    }
});
