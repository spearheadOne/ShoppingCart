const { cartUiUrl, productUiUrl } = window.shoppingCartConfig;

const cartFrame = document.getElementById("cart-ui");
const productFrame = document.getElementById("product-ui");

cartFrame.src = cartUiUrl;
productFrame.src = productUiUrl;
document.getElementById("cart-link").href = cartUiUrl;
document.getElementById("product-link").href = productUiUrl;

window.addEventListener("message", (event) => {
    if (
        event.source === productFrame.contentWindow
        && event.data?.type === "shopping-cart:changed"
    ) {
        cartFrame.contentWindow.postMessage(event.data, new URL(cartUiUrl).origin);
    }

    if (
        event.source === productFrame.contentWindow
        && event.data?.type === "shopping-cart:request-state"
    ) {
        cartFrame.contentWindow.postMessage(event.data, new URL(cartUiUrl).origin);
    }

    if (
        event.source === cartFrame.contentWindow
        && event.data?.type === "shopping-cart:state"
    ) {
        productFrame.contentWindow.postMessage(event.data, new URL(productUiUrl).origin);
    }
});
