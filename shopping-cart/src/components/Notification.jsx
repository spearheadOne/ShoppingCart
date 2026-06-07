import React from "react";

import "../stylesheets/Notification.css"
import { Alert } from "@mui/material"
import cartStore from "../store/cart-store";


function Notification(props) {
    const notification = cartStore(state => state.notification)
    const showNotification = cartStore(state => state.showNotification)

    function handleClose(){
        showNotification({
            open:false
        });
    }


    return (
        <div>

            {
                notification.open && 
                
                <Alert severity={props.type} onClose={handleClose}>
                    {props.message}
                </Alert>
            }

        </div>
    )
}

export default Notification;
