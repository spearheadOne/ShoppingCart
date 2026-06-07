import React from "react";

import "../stylesheets/Notification.css"
import { Alert } from "@mui/material"
import { useDispatch, useSelector } from "react-redux";
import { notificationActions } from "../store/notification-slice";


function Notification(props) {
    const notification = useSelector(state => state.notification.notification)
    const dispatch = useDispatch()

    function handleClose(){
        dispatch(notificationActions.showNotification({
            open:false
        }));
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