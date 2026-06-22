import Cart from "./Cart";
import "../stylesheets/Header.css";

function Header() {
    return (
        <header className="header">
            <div className="header-brand">
                <span className="brand-mark" aria-hidden="true">SC</span>
                <div>
                    <strong>Cart workspace</strong>
                    <span>Shopping cart API demo</span>
                </div>
            </div>
            <Cart />
        </header>
    );
}

export default Header;
