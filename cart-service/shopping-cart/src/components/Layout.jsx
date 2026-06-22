import Header from "./Header";
import CartItemList from "./CartItemList";
import "../stylesheets/Layout.css";

function Layout() {
    return (
        <div className="app-shell">
            <Header />
            <main className="layout">
                <section className="cart-panel" aria-label="Shopping cart">
                    <CartItemList />
                </section>
            </main>
        </div>
    );
}

export default Layout;
