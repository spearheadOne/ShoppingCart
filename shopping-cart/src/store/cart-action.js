import { cartActions } from "./cart-slice";
import { notificationActions } from "./notification-slice";

export const sendCartData = (cart) => {
    return async (dispatch) => {
        const sendRequest = async () => {
            dispatch(notificationActions.showNotification({
                open: true,
                message: "Sending request",
                type: 'warning'
            }));

            const resp = await fetch(
                "https://shoppingcart-a62bb-default-rtdb.europe-west1.firebasedatabase.app/cartItems.json",
                {
                    method: "PUT",
                    body: JSON.stringify(cart)
                });

            const data = await resp.json;

            dispatch(notificationActions.showNotification({
                open: true,
                message: "Data sent succesfully",
                type: 'success'
            }));
        }

        try {
            await sendRequest()
        } catch (err) {
            dispatch(notificationActions.showNotification({
                open: true,
                message: "Error sending data",
                type: 'error'
            }));
        }
    }
}


export const fetchCartData = () => {
    return async (dispatch) => {
        const fetchHandler = async () => {
            const resp = await fetch("https://shoppingcart-a62bb-default-rtdb.europe-west1.firebasedatabase.app/cartItems.json")
            const data = await resp.json();
            
        }

        try {
            const cartData = await fetchHandler();
            dispatch(cartActions.replaceData(cartData))
        } catch (err) {
            dispatch(notificationActions.showNotification({
                open: true,
                message: "Error sending data",
                type: 'error'
            }));
        }
    }
}