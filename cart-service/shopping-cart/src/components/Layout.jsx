import Header from "./Header";
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

            </main>
        </div>
    );
}

export default Layout;
