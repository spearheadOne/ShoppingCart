const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const { buildRuntimeConfigScript, createApp } = require("./server");

test("builds runtime UI configuration", () => {
    const script = buildRuntimeConfigScript(
        "http://cart.example",
        "http://product.example"
    );

    assert.equal(
        script,
        "window.shoppingCartConfig={\"cartUiUrl\":\"http://cart.example\","
        + "\"productUiUrl\":\"http://product.example\"};"
    );
});

test("creates an Express application with the combined UI entry point", () => {
    const app = createApp();
    const index = fs.readFileSync(
        path.join(__dirname, "public", "index.html"),
        "utf8"
    );

    assert.equal(typeof app, "function");
    assert.match(index, /Products and cart/);
    assert.match(index, /\/config\.js/);
    assert.match(index, /\/app\.js/);
});

test("combined UI script retains and replays cart synchronization state", () => {
    const script = fs.readFileSync(
        path.join(__dirname, "public", "app.js"),
        "utf8"
    );

    assert.match(script, /shopping-cart:synchronize/);
    assert.match(script, /shopping-cart:cleared/);
    assert.match(script, /addEventListener\("load"/);
    assert.match(script, /localStorage\.setItem/);
    assert.doesNotMatch(script, /cart-link|product-link/);
    assert.doesNotMatch(script, /setInterval/);
});
