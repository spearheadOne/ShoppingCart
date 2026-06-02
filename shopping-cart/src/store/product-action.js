import { productActions } from "./product-slice";
import { notificationActions } from "./notification-slice";


export const fetchProducts = () => {
    return async (dispatch) => {
        const fetchHandler = async () => {
            const resp = await fetch("https://shoppingcart-a62bb-default-rtdb.europe-west1.firebasedatabase.app/products.json")
            const data = await resp.json();
            return data;
            
        }

        try {
            const productData = await fetchHandler();
            dispatch(productActions.replaceData(productData))
        } catch (err) {
            dispatch(notificationActions.showNotification({
                open: true,
                message: "Error reading product data",
                type: 'error'
            }));
        }
    }
}

