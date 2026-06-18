import Header from "./Header";
import ProductList from "./ProductList";
import CartItemList from "./CartItemList";
import "../stylesheets/Layout.css";

function Layout() {
    return (
        <div className="app-shell">
            <Header />
            <main className="layout">
                <section className="cart-panel" aria-labelledby="cart-workspace-title">
                    <div className="section-heading">
                        <div>
                            <span className="eyebrow">Cart management</span>
                            <h1 id="cart-workspace-title">Review your cart</h1>
                        </div>
                        <p>Review contents, set exact quantities, remove items, or delete the cart.</p>
                    </div>
                    <CartItemList />
                </section>

                <section className="catalog-panel" aria-labelledby="catalog-title">
                    <div className="section-heading catalog-heading">
                        <div>
                            <span className="eyebrow">Demo catalog</span>
                            <h2 id="catalog-title">Add products</h2>
                        </div>
                        <p>The catalog is kept separate from your active cart.</p>
                    </div>
                    <ProductList />
                </section>
            </main>
        </div>
    );
}

export default Layout;
