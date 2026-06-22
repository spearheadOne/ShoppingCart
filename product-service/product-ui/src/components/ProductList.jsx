import Product from "./Product";
import productStore from "../store/product-store";
import "../stylesheets/ProductList.css";

function ProductList() {
    const products = productStore((state) => state.products);
    const productsLoading = productStore((state) => state.productsLoading);

    if (productsLoading) {
        return <div className="catalog-state">Loading products…</div>;
    }

    if (!products.length) {
        return <div className="catalog-state">No products are available.</div>;
    }

    return (
        <ul className="product-list">
            {products.map((product) => (
                <li key={product.id}>
                    <Product {...product} imgUrl={product.imgURL} />
                </li>
            ))}
        </ul>
    );
}

export default ProductList;
