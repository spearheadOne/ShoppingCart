const express = require("express");
const path = require("node:path");

function buildRuntimeConfigScript(cartUiUrl, productUiUrl) {
    return `window.shoppingCartConfig=${JSON.stringify({ cartUiUrl, productUiUrl })};`;
}

function createApp({
    cartUiUrl = process.env.CART_UI_URL || "http://localhost:8080",
    productUiUrl = process.env.PRODUCT_UI_URL || "http://localhost:8081"
} = {}) {
    const app = express();
    const publicDir = path.join(__dirname, "public");

    app.disable("x-powered-by");

    app.get("/health", (request, response) => {
        response.json({ status: "UP" });
    });

    app.get("/config.js", (request, response) => {
        response.type("application/javascript")
                .send(buildRuntimeConfigScript(cartUiUrl, productUiUrl));
    });

    app.use(express.static(publicDir));

    app.get("*path", (request, response) => {
        response.sendFile(path.join(publicDir, "index.html"));
    });

    return app;
}

if (require.main === module) {
    const port = Number(process.env.PORT || 3000);
    createApp().listen(port, () => {
        console.log(`Combined UI listening on http://localhost:${port}`);
    });
}

module.exports = { buildRuntimeConfigScript, createApp };
