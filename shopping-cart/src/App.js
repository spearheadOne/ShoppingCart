import { useSelector, useDispatch } from "react-redux"
import Login from "./components/Login"
import Layout from "./components/Layout";
import { useEffect } from "react";
import Notification from "./components/Notification";

import { fetchCartData, sendCartData } from "./store/cart-action";
import {fetchProducts} from "./store/product-action";

let isFirstRender = true
function App() {

  const isLoggedIn = useSelector(state => state.login.isLoggedIn);
  const cart = useSelector(state => state.cart);
  const dispatch = useDispatch();
  const notification = useSelector(state => state.notification.notification)


   useEffect(()=>{
     dispatch(fetchCartData())
     dispatch(fetchProducts())
   },[dispatch]);


  useEffect(() => {

    if (isFirstRender) {
      isFirstRender = false;
      return;
    }

    if (cart.changed){
      dispatch(sendCartData(cart));
    }
 
  }, [cart,dispatch]);

  return (
    <div>
      {notification && <Notification type={notification.type} message={notification.message} />}
      {!isLoggedIn && <Login />}
      {isLoggedIn && <Layout />}

    </div>
  );

}


export default App;
