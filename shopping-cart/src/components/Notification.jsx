import { Alert } from "@mui/material";
import cartStore from "../store/cart-store";
import "../stylesheets/Notification.css";

function Notification({ type, message }) {
    const showNotification = cartStore((state) => state.showNotification);

    return (
        <div className="notification">
            <Alert severity={type} onClose={() => showNotification({ open: false })}>
                {message}
            </Alert>
        </div>
    );
}

export default Notification;
