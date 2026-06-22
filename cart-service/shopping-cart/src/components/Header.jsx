import Cart from "./Cart";
import "../stylesheets/Header.css";

function Header() {
    return (
        <header className="header">
            <div className="header-brand">
                <strong>Shopping cart</strong>
            </div>
            <Cart />
        </header>
    );
}

export default Header;
